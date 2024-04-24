package com.edelweiss.playerex.listeners

import com.edelweiss.playerex.PlayerEXComponents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

object ServerEventListeners {
    fun serverStarting(server: MinecraftServer) {

    }

    /** Before a player respawns, copy old data to another player. */
    fun reset(oldPlayer: ServerPlayerEntity, newPlayer: ServerPlayerEntity, alive: Boolean) {
        PlayerEXComponents.playerEntities.get(newPlayer)
    }
}