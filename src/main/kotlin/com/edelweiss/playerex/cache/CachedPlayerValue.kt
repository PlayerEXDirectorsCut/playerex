package com.edelweiss.playerex.cache

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

interface CachedPlayerValue<V> {
    /**
     * Used to get the cached value from the player.
     * */
    fun get(player: ServerPlayerEntity): V

    /** Reads a value from a nbt. */
    fun readFromNbt(tag: NbtCompound): V

    /** Writes a value to a nbt. */
    fun writeToNbt(tag: NbtCompound, value: Any?): Boolean

    /** The key of the value. This would be used in the form `modid:<path>`. */
    fun id(): Identifier
}