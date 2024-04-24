package com.edelweiss.playerex.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object PlayerEXCommands {
    fun registerHead(dispatcher: CommandDispatcher<ServerCommandSource>): LiteralCommandNode<ServerCommandSource> {
        val head = CommandManager.literal("playerex").requires { source -> source.hasPermissionLevel(2) }.build()

        // add playerex commands

        dispatcher.root.addChild(head)

        return head
    }
}