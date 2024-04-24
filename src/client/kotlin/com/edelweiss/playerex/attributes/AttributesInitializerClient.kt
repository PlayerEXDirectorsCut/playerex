package com.edelweiss.playerex.attributes

import com.edelweiss.playerex.PlayerEXDirectorsCut
import com.edelweiss.playerex.attributes.networking.Packets
import net.minecraft.nbt.NbtCompound

object AttributesInitializerClient {
    fun updateManagerNBT(nbt: NbtCompound) {
        DataAttributes.MANAGER.readFromNbt(nbt)
        DataAttributes.MANAGER.apply()
    }

    fun initialize() {
        PlayerEXDirectorsCut.CHANNEL.registerClientbound(Packets.Handshake::class.java) { packet, access -> updateManagerNBT(packet.nbt) }
        PlayerEXDirectorsCut.CHANNEL.registerClientbound(Packets.Reload::class.java) { packet, access -> updateManagerNBT(packet.nbt) }
    }
}