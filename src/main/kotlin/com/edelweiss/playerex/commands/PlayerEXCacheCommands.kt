package com.edelweiss.playerex.commands

import com.edelweiss.playerex.cache.PlayerEXCache
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.datafixers.util.Either
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.command.argument.UuidArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.UUID
import kotlin.math.abs

object PlayerEXCacheCommands {
    private val suggestionKeys = SuggestionProvider<ServerCommandSource> { _, builder ->
        CommandSource.suggestIdentifiers(PlayerEXCache.keys(), builder)
    }
    private val suggestionNames = SuggestionProvider<ServerCommandSource> { ctx, builder ->
        val cache = PlayerEXCache.get(ctx.source.server) ?: return@SuggestionProvider builder.buildFuture();
        cache.playerNames(ctx.source.server).forEach(builder::suggest)
        return@SuggestionProvider builder.buildFuture()
    }
    private val suggestionUUIDs = SuggestionProvider<ServerCommandSource> { ctx, builder ->
        val cache = PlayerEXCache.get(ctx.source.server) ?: return@SuggestionProvider builder.buildFuture()
        cache.playerIDs(ctx.source.server).forEach { id -> builder.suggest(id.toString()) }
        return@SuggestionProvider builder.buildFuture()
    }

    private fun <T>nullKeyMessage(id: T): () -> MutableText = { Text.literal("$id -> <null_key>").formatted(Formatting.RED) }

    private fun playerIDMessage(cache: PlayerEXCache, either: Either<String, UUID>?, text: () -> MutableText): () -> MutableText = {
        var formattedID = "<invalid_id>"
        either?.ifLeft { name ->
            formattedID = "UUID: ${cache.getUUID(name)}\n" + "Name: $name"
        }?.ifRight {
            uuid -> formattedID = "UUID: $uuid\n" + "Name: ${cache.getPlayerName(uuid)}"
        }
        Text.literal("$formattedID\n").formatted(Formatting.GREEN)
            .append(text())
    }

    private fun <T>getKey(input: (CommandContext<ServerCommandSource>) -> T): ArgumentCommandNode<ServerCommandSource, Identifier> {
        return CommandManager.argument("key", IdentifierArgumentType.identifier()).suggests(suggestionKeys).executes { ctx ->
            val id = input(ctx)
            val identifier = IdentifierArgumentType.getIdentifier(ctx, "key")
            val value = PlayerEXCache.getKey(identifier)

            if (value == null) {
                ctx.source.sendFeedback(nullKeyMessage(id), false)
                return@executes -1
            }

            val server = ctx.source.server

            val cache = PlayerEXCache.get(server) ?: return@executes -1;

            val uuidOrString: Either<String, UUID>? = if (id is String) Either.left(id) else { if (id is UUID) Either.right(id) else null }
            var fetchedValue: Any? = null

            uuidOrString?.ifLeft { fetchedValue = cache.get(server, id as String, value) }
            uuidOrString?.ifRight { fetchedValue = cache.get(server, id as UUID, value) }

            ctx.source.sendFeedback(
                playerIDMessage(cache, uuidOrString) { Text.literal("[$identifier] is ($fetchedValue)").formatted(Formatting.WHITE) },
                false
            )

            if (fetchedValue is Number) return@executes abs(fetchedValue as Int) % 16

            return@executes 1
        }.build()
    }

    private fun <T>removeKey(input: (CommandContext<ServerCommandSource>) -> T): ArgumentCommandNode<ServerCommandSource, Identifier> {
        return CommandManager.argument("key", IdentifierArgumentType.identifier()).suggests(suggestionKeys).executes { ctx ->
            val id = input(ctx)
            val identifier = IdentifierArgumentType.getIdentifier(ctx, "key")
            val value = PlayerEXCache.getKey(identifier)

            if (value == null) {
                ctx.source.sendFeedback(nullKeyMessage(id), false)
                return@executes -1
            }

            val server = ctx.source.server

            val cache = PlayerEXCache.get(server) ?: return@executes -1;
            if (id is String) cache.uncache(id as String)
            else if (id is UUID) cache.uncache(id as UUID)

            ctx.source.sendFeedback({Text.literal("-$id -$identifier").formatted(Formatting.GRAY)}, false)

            return@executes 1
        }.build()
    }

    private fun get(root: LiteralCommandNode<ServerCommandSource>) {
        val getNode = CommandManager.literal("get").build()
        val nameNode = CommandManager.literal("name").build()
        val uuidNode = CommandManager.literal("uuid").build()

        val nameArgNode = CommandManager.argument("name", StringArgumentType.string()).suggests(suggestionNames).build()
        val key1 = getKey { ctx -> StringArgumentType.getString(ctx, "name") }
        val uuid = CommandManager.argument("uuid", UuidArgumentType.uuid()).suggests(suggestionUUIDs).build()
        val key2 = getKey { ctx -> UuidArgumentType.getUuid(ctx, "uuid") }

        root.addChild(getNode)
        getNode.addChild(nameNode)
        getNode.addChild(uuidNode)

        nameNode.addChild(nameArgNode)
        uuidNode.addChild(uuid)
        nameArgNode.addChild(key1)
        uuid.addChild(key2)
    }

    private fun remove(root: LiteralCommandNode<ServerCommandSource>) {
        val removeNode = CommandManager.literal("remove").build()
        val nameNode = CommandManager.literal("name").build()
        val uuidNode = CommandManager.literal("uuid").build()

        val nameArgNode = CommandManager.argument("name", StringArgumentType.string()).suggests(suggestionNames).executes {ctx ->
            val server = ctx.source.server
            val cache = PlayerEXCache.get(server) ?: return@executes -1;
            val playerString = StringArgumentType.getString(ctx, "name")
            cache.uncache(playerString)
            ctx.source.sendFeedback({Text.literal("-$playerString -*").formatted(Formatting.GRAY)}, false)
            return@executes 1
        }.build()

        val key1 = removeKey { ctx -> StringArgumentType.getString(ctx, "name") }
        val uuid = CommandManager.argument("uuid", UuidArgumentType.uuid()).suggests(suggestionUUIDs).executes { ctx ->
            val server = ctx.source.server
            val cache = PlayerEXCache.get(server) ?: return@executes -1;
            val playerUUID = UuidArgumentType.getUuid(ctx, "uuid")
            cache.uncache(playerUUID)
            ctx.source.sendFeedback({Text.literal("-$playerUUID -*").formatted(Formatting.GRAY)}, false)
            return@executes 1
        }.build()

        val key2 = removeKey { ctx -> UuidArgumentType.getUuid(ctx, "uuid") }

        root.addChild(removeNode)
        removeNode.addChild(nameNode)
        removeNode.addChild(uuidNode)
        nameNode.addChild(nameArgNode)
        uuidNode.addChild(uuid)
        nameArgNode.addChild(key1)
        uuid.addChild(key2)
    }

    fun register(head: LiteralCommandNode<ServerCommandSource>) {
        val root = CommandManager.literal("cache").requires { source -> source.hasPermissionLevel(2) }.build()
        head.addChild(root)
        get(root)
        remove(root)
    }
}