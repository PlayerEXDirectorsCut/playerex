package com.edelweiss.playerex.attributes.networking

import net.minecraft.nbt.NbtCompound

object Packets {
    @JvmRecord
    data class Handshake(val nbt: NbtCompound)

    @JvmRecord
    data class Reload(val nbt: NbtCompound)
}