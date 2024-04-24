package com.edelweiss.playerex.cache

import com.edelweiss.playerex.PlayerEXDirectorsCut
import net.minecraft.server.MinecraftServer
import java.util.UUID

/**
 * The API for the PlayerEX Cache.
 *
 * For static members, you can register values into the API.
 *
 * In order to utilize it in its full capacity, the server needs to be passed in the constructor.
 * */
class PlayerEXCacheAPI(private val server: MinecraftServer) {
    companion object {
        /** Registers the provided cached value to the server. */
        fun <V>register(key: CachedPlayerValue<V>): CachedPlayerValue<V> {
            PlayerEXDirectorsCut.LOGGER.debug("@cache: value registered: <{} :: #id: {}>", key, key.id())
            return PlayerEXCache.register(key)
        }
    }

    /** Fetches the last cached value if offline, otherwise will fetch the current value from the player. */
    fun <V>get(uuid: UUID, key: CachedPlayerValue<V>): V? = PlayerEXCache.get(this.server)!!.get(server, uuid, key)

    /** Fetches the last cached value if offline, otherwise will fetch the current value from the player. */
    fun <V>get(playerName: String, key: CachedPlayerValue<V>): V? = PlayerEXCache.get(this.server)!!.get(server, playerName, key)

    /** Returns all offline & online player UUIDs. */
    fun playerIDs(): Collection<UUID> = PlayerEXCache.get(this.server)!!.playerIDs(this.server)

    /** Returns all offline & online player names. */
    fun playerNames(): Collection<String> = PlayerEXCache.get(this.server)!!.playerNames(this.server)

    /** Checks if the player with the UUID is in the cache or not. */
    fun isPlayerCached(uuid: UUID): Boolean = PlayerEXCache.get(this.server)!!.isPlayerCached(uuid)

    /** Checks if the player with the name is in the cache or not. */
    fun isPlayerCached(playerName: String): Boolean = PlayerEXCache.get(this.server)!!.isPlayerCached(playerName)
}