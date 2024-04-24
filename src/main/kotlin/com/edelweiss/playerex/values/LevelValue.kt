package com.edelweiss.playerex.values

import com.edelweiss.playerex.api.PlayerEXAPI
import com.edelweiss.playerex.cache.CachedPlayerValue
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
/** Represents a cacheable PlayerEX level. */
class LevelValue(private val id: Identifier = PlayerEXAPI.id("level")) : CachedPlayerValue<Int> {
    override fun get(player: ServerPlayerEntity): Int {
        return 0 // todo: requires data attributes
    }

    override fun readFromNbt(tag: NbtCompound): Int {
        return tag.getInt("level")
    }

    override fun writeToNbt(tag: NbtCompound, value: Any?): Boolean {
        if (value is Int) {
            tag.putInt(this.id.path, value)
            return true
        }
        else return false
    }

    override fun id(): Identifier = this.id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null) return false
        if (other !is LevelValue) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = this.id.hashCode()

    override fun toString(): String = this.id.toString()
}