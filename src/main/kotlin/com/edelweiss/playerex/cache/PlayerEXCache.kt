package com.edelweiss.playerex.cache

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.UUID

/** This is the singleton class that implements the cache. */
class PlayerEXCache(
    private val cache: MutableMap<UUID, MutableMap<CachedPlayerValue<*>, *>> = mutableMapOf(),
    private val playerNameToUUID: BiMap<String, UUID> = HashBiMap.create()
) {
    companion object {
        private val cacheKeys = mutableMapOf<Identifier, CachedPlayerValue<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <V>register(key: CachedPlayerValue<V>): CachedPlayerValue<V> {
            return cacheKeys.computeIfAbsent(key.id()) { key } as CachedPlayerValue<V>
        }

        fun keys(): Set<Identifier> = cacheKeys.keys

        fun getKey(id: Identifier): CachedPlayerValue<*>? = cacheKeys[id]

        /** Tries to obtain the cache from the `LevelProperties` of the server, and if it is not found, it will be null. */
        fun get(server: MinecraftServer): PlayerEXCache? {
            val worldProperties = server.overworld.levelProperties
            return (worldProperties as PlayerEXCacheData?)?.playerEXCache()
        }
    }

    private fun isValidPlayerData(player: ServerPlayerEntity, function: (UUID, String) -> Unit): Boolean {
        val profile = player.gameProfile ?: return false
        if (profile.id == null || profile.name == null) return false
        function(profile.id, profile.name)
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun <V>getFromCache(uuid: UUID, key: CachedPlayerValue<V>): V? = this.cache[uuid]?.get(key) as V?

    /** Gets a cached value from a players `UUID` and a cached value key.*/
    internal fun <V>get(server: MinecraftServer, uuid: UUID, key: CachedPlayerValue<V>): V? {
        val player = server.playerManager.getPlayer(uuid)
        return if (player == null) this.getFromCache(uuid, key) else key.get(player)
    }

    /** Gets a cached value from a players name and a cached value key. */
    internal fun <V>get(server: MinecraftServer, playerName: String, key: CachedPlayerValue<V>): V? {
        if (playerName.isEmpty()) return null
        val player = server.playerManager.getPlayer(playerName)
        if (player == null) {
            val uuid = this.playerNameToUUID[playerName]?: return null
            return this.getFromCache(uuid, key)
        }
        return key.get(player)
    }

    /** Collect all the player ids from the server into a `Set`. */
    fun playerIDs(server: MinecraftServer): Collection<UUID> {
        val set = HashSet<UUID>(this.playerNameToUUID.values)
        for (player in server.playerManager.playerList) {
            val uuid = player?.gameProfile?.id ?: continue
            set.add(uuid)
        }
        return set
    }

    /** Attempts to get the player UUID within the cache map based on a name. */
    fun getUUID(playerName: String) = this.playerNameToUUID[playerName]

    /** Attempts to get the player name within the cache map based on a UUID. */
    fun getPlayerName(uuid: UUID) = this.playerNameToUUID.inverse()[uuid]

    /** Collects all the player names from the server into a `Set`. */
    fun playerNames(server: MinecraftServer): Collection<String> {
        val set = HashSet<String>(this.playerNameToUUID.keys)
        for (player in server.playerManager.playerList) {
            val playerName = player?.gameProfile?.name
            if (!playerName.isNullOrEmpty()) set.add(playerName)
        }
        return set
    }

    fun writeToNbt(): NbtList {
        val list = NbtList()
        val uuidToPlayerNames = this.playerNameToUUID.inverse()

        for (uuid in this.cache.keys) {
            val data = this.cache[uuid] ?: continue

            val entry = NbtCompound()
            val keys = NbtCompound()

            entry.putUuid(PlayerCacheKeys.UUID, uuid)
            entry.putString(PlayerCacheKeys.NAME, uuidToPlayerNames.getOrDefault(uuid, ""))

            for (key in data.keys) {
                val innerEntry = NbtCompound()
                key.writeToNbt(innerEntry, data[key])
                keys.put(key.id().toString(), innerEntry)
            }

            entry.put(PlayerCacheKeys.KEYS, keys)
            list.add(entry)
        }

        return list
    }

    fun readFromNbt(list: NbtList) {
        if (list.isEmpty()) return

        this.cache.clear()
        this.playerNameToUUID.clear()

        for (index in list.indices) {
            val entry = list.getCompound(index)
            val keysCompound = entry.getCompound(PlayerCacheKeys.KEYS)
            val uuid = entry.getUuid(PlayerCacheKeys.UUID)
            val name = entry.getString(PlayerCacheKeys.NAME)

            if (name.isEmpty()) continue

            val data = mutableMapOf<CachedPlayerValue<*>, Any>()

            for (id in keysCompound.keys) {
                val key = cacheKeys[Identifier(id)]
                val value = key?.readFromNbt(keysCompound.getCompound(id)) ?: continue
                data[key] = value
            }

            this.cache[uuid] = data
            this.playerNameToUUID[name] = uuid
        }
    }

    fun isPlayerCached(uuid: UUID) = this.cache.containsKey(uuid)

    fun isPlayerCached(name: String) = this.playerNameToUUID.containsKey(name)

    fun cache(player: ServerPlayerEntity): Boolean = this.isValidPlayerData(player) { uuid, playerName ->
        val value = mutableMapOf<CachedPlayerValue<*>, Any?>()
        cacheKeys.values.forEach { key -> value[key] = key.get(player) }
        this.cache[uuid] = value
        this.playerNameToUUID[playerName] = uuid
    }

    /** Un-caches the player based on the provided `ServerPlayerEntity`. */
    fun uncache(player: ServerPlayerEntity) = this.isValidPlayerData(player) { uuid, _ ->
        this.cache.remove(uuid)
        this.playerNameToUUID.inverse().remove(uuid)
    }

    /**
     * Attempts to un-cache the player using a `UUID` and a given key.
     *
     * This could fail if the UUID is not in the cache, or the key removed is not present in the cache.
     */
    fun uncache(uuid: UUID, key: CachedPlayerValue<*>): Boolean {
        if (cacheKeys.containsKey(key.id())) return false

        val value = this.cache[uuid] ?: return false

        if (value.remove(key) != null) {
            if (value.isEmpty()) {
                this.cache.remove(uuid)
                this.playerNameToUUID.inverse().remove(uuid)
            }
            return true
        }
        return false
    }

    /**
     * Attempts to un-cache the player using their name and a given key.
     *
     * This could fail if the player name is not in the cache, or the key removed is not present in the cache.
     */
    fun uncache(playerName: String, key: CachedPlayerValue<*>): Boolean {
        if (playerName.isEmpty()) return false
        return this.uncache(this.playerNameToUUID[playerName] ?: return false, key)
    }


    /**
     * Attempts to un-cache the player using a `UUID`.
     *
     * This could fail if the UUID is not in the cache.
     */
    fun uncache(uuid: UUID): Boolean {
        this.cache.remove(uuid)
        return this.playerNameToUUID.inverse().remove(uuid) != null
    }

    /**
     * Attempts to un-cache the player using their name.
     *
     * This could fail if the UUID is not in the cache.
     */
    fun uncache(playerName: String): Boolean {
        if (playerName.isEmpty()) return false
        val uuid = this.playerNameToUUID[playerName] ?: return false
        return this.uncache(uuid)
    }
}