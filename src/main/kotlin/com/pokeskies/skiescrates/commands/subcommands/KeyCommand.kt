package com.pokeskies.skiescrates.commands.subcommands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.commands.KeysCommand
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.managers.KeyManager
import com.pokeskies.skiescrates.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class KeyCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("key")
            .requires(Permissions.require("${SkiesCrates.MOD_ID}.command.key", 2))
            .then(Commands.literal("give")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("key", StringArgumentType.string())
                        .suggests { context, builder ->
                            ConfigManager.KEYS.forEach { builder.suggest(it.key) }
                            builder.buildFuture()
                        }
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .then(Commands.argument("silent", BoolArgumentType.bool())
                                .executes { ctx: CommandContext<CommandSourceStack> ->
                                    give(
                                        ctx,
                                        EntityArgument.getPlayers(ctx, "targets").toList(),
                                        StringArgumentType.getString(ctx, "key"),
                                        IntegerArgumentType.getInteger(ctx, "amount"),
                                        BoolArgumentType.getBool(ctx, "silent")
                                    )
                                }
                            )
                            .executes { ctx: CommandContext<CommandSourceStack> ->
                                give(
                                    ctx,
                                    EntityArgument.getPlayers(ctx, "targets").toList(),
                                    StringArgumentType.getString(ctx, "key"),
                                    IntegerArgumentType.getInteger(ctx, "amount")
                                )
                            }
                        )
                        .executes { ctx: CommandContext<CommandSourceStack> ->
                            give(
                                ctx,
                                EntityArgument.getPlayers(ctx, "targets").toList(),
                                StringArgumentType.getString(ctx, "key")
                            )
                        }
                    )
                )
            )
            .then(Commands.literal("take")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("key", StringArgumentType.string())
                        .suggests { context, builder ->
                            ConfigManager.KEYS.forEach { builder.suggest(it.key) }
                            builder.buildFuture()
                        }
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .then(Commands.argument("silent", BoolArgumentType.bool())
                                .executes { ctx: CommandContext<CommandSourceStack> ->
                                    take(
                                        ctx,
                                        EntityArgument.getPlayers(ctx, "targets").toList(),
                                        StringArgumentType.getString(ctx, "key"),
                                        IntegerArgumentType.getInteger(ctx, "amount"),
                                        BoolArgumentType.getBool(ctx, "silent")
                                    )
                                }
                            )
                            .executes { ctx: CommandContext<CommandSourceStack> ->
                                take(
                                    ctx,
                                    EntityArgument.getPlayers(ctx, "targets").toList(),
                                    StringArgumentType.getString(ctx, "key"),
                                    IntegerArgumentType.getInteger(ctx, "amount")
                                )
                            }
                        )
                        .executes { ctx: CommandContext<CommandSourceStack> ->
                            take(
                                ctx,
                                EntityArgument.getPlayers(ctx, "targets").toList(),
                                StringArgumentType.getString(ctx, "key")
                            )
                        }
                    )
                )
            )
            .then(Commands.literal("set")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("key", StringArgumentType.string())
                        .suggests { context, builder ->
                            ConfigManager.KEYS.forEach { builder.suggest(it.key) }
                            builder.buildFuture()
                        }
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .then(Commands.argument("silent", BoolArgumentType.bool())
                                .executes { ctx: CommandContext<CommandSourceStack> ->
                                    set(
                                        ctx,
                                        EntityArgument.getPlayers(ctx, "targets").toList(),
                                        StringArgumentType.getString(ctx, "key"),
                                        IntegerArgumentType.getInteger(ctx, "amount"),
                                        BoolArgumentType.getBool(ctx, "silent")
                                    )
                                }
                            )
                            .executes { ctx: CommandContext<CommandSourceStack> ->
                                set(
                                    ctx,
                                    EntityArgument.getPlayers(ctx, "targets").toList(),
                                    StringArgumentType.getString(ctx, "key"),
                                    IntegerArgumentType.getInteger(ctx, "amount")
                                )
                            }
                        )
                        .executes { ctx: CommandContext<CommandSourceStack> ->
                            set(
                                ctx,
                                EntityArgument.getPlayers(ctx, "targets").toList(),
                                StringArgumentType.getString(ctx, "key")
                            )
                        }
                    )
                )
            )
            .then(Commands.literal("view")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes { ctx -> KeysCommand.execute(ctx, EntityArgument.getPlayer(ctx, "target"))}
                )
                .executes { ctx -> KeysCommand.execute(ctx, ctx.source.playerOrException)}
            )
            .executes { ctx -> KeysCommand.execute(ctx, ctx.source.playerOrException)}
            .build()
    }

    companion object {
        // This is key to ensuring that we don't have multiple threads trying to give keys at the same time
        // A ConcurrentHashMap is used because it provides thread safety and ANY is the lock object itself
        private val dataLocks = ConcurrentHashMap<UUID, Any>()

        fun give(
            ctx: CommandContext<CommandSourceStack>,
            targets: List<ServerPlayer>,
            keyId: String,
            amount: Int = 1,
            silent: Boolean = false
        ): Int {
            val key = ConfigManager.KEYS[keyId] ?: run {
                ctx.source.sendMessage(Component.text("Key $keyId could not be found!").color(NamedTextColor.RED))
                return 0
            }

            val results = targets.map { player ->
                val lock = dataLocks.computeIfAbsent(player.uuid) { Any() }
                CompletableFuture.supplyAsync {
                    synchronized(lock) {
                        KeyManager.giveKeys(key, player, amount, silent).join()
                    }
                }
            }

            CompletableFuture.allOf(*results.toTypedArray()).thenAccept {
                val successful = results.count { it.join() }
                when (successful) {
                    0 -> ctx.source.sendMessage(
                        Component.text("Failed to give ${amount}x $keyId keys to any players!")
                            .color(NamedTextColor.RED)
                    )

                    targets.size -> ctx.source.sendMessage(
                        Component.text("Successfully gave ${amount}x $keyId keys to $successful players!")
                            .color(NamedTextColor.GREEN),
                    )

                    else -> ctx.source.sendMessage(
                        Component.text(
                            "Successfully gave ${amount}x $keyId keys to $successful player(s), " +
                                    "but failed to give it to ${targets.size - successful} player(s)!"
                        ).color(NamedTextColor.YELLOW),
                    )
                }
            }

            return 1
        }

        fun take(
            ctx: CommandContext<CommandSourceStack>,
            targets: List<ServerPlayer>,
            keyId: String,
            amount: Int = 1,
            silent: Boolean = false
        ): Int {
            val key = ConfigManager.KEYS[keyId] ?: run {
                ctx.source.sendMessage(Component.text("Key $keyId could not be found!").color(NamedTextColor.RED))
                return 0
            }

            // TODO: Make this support non virtual keys
            if (!key.virtual) {
                ctx.source.sendMessage(Component.text("Key $keyId is not a virtual key!").color(NamedTextColor.RED))
                return 0
            }

            val results = targets.map { player ->
                val lock = dataLocks.computeIfAbsent(player.uuid) { Any() }
                CompletableFuture.supplyAsync {
                    synchronized(lock) {
                        KeyManager.takeKeys(key, player, amount, silent).join()
                    }
                }
            }

            CompletableFuture.allOf(*results.toTypedArray()).thenAccept {
                val successful = results.count { it.join() }
                when (successful) {
                    0 -> ctx.source.sendMessage(
                        Component.text("Failed to take ${amount}x $keyId keys from players!").color(NamedTextColor.RED)
                    )

                    targets.size -> ctx.source.sendMessage(
                        Component.text("Successfully took ${amount}x $keyId keys from $successful players!")
                            .color(NamedTextColor.GREEN),
                    )

                    else -> ctx.source.sendMessage(
                        Component.text(
                            "Successfully took ${amount}x $keyId keys from $successful player(s), " +
                                    "but failed to take from ${targets.size - successful} player(s)!"
                        ).color(NamedTextColor.YELLOW),
                    )
                }
            }

            return 1
        }

        fun set(
            ctx: CommandContext<CommandSourceStack>,
            targets: List<ServerPlayer>,
            keyId: String,
            amount: Int = 1,
            silent: Boolean = false
        ): Int {
            val key = ConfigManager.KEYS[keyId] ?: run {
                ctx.source.sendMessage(Component.text("Key $keyId could not be found!").color(NamedTextColor.RED))
                return 0
            }

            // TODO: Make this support non virtual keys
            if (!key.virtual) {
                ctx.source.sendMessage(Component.text("Key $keyId is not a virtual key!").color(NamedTextColor.RED))
                return 0
            }

            if (amount <= 0) {
                ctx.source.sendMessage(Component.text("Amount must be greater than 0!").color(NamedTextColor.RED))
                return 0
            }

            val results = targets.map { player ->
                val lock = dataLocks.computeIfAbsent(player.uuid) { Any() }
                CompletableFuture.supplyAsync {
                    synchronized(lock) {
                        KeyManager.setKeys(key, player, amount, silent).join()
                    }
                }
            }

            CompletableFuture.allOf(*results.toTypedArray()).thenAccept {
                val successful = results.count { it.join() }
                when (successful) {
                    0 -> ctx.source.sendMessage(
                        Component.text("Failed to set ${amount}x $keyId keys for players!").color(NamedTextColor.RED)
                    )
                    targets.size -> ctx.source.sendMessage(
                        Component.text("Successfully set ${amount}x $keyId keys for $successful players!").color(NamedTextColor.GREEN),
                    )
                    else -> ctx.source.sendMessage(
                        Component.text("Successfully set ${amount}x $keyId keys for $successful player(s), " +
                                "but failed to set for ${targets.size - successful} player(s)!").color(NamedTextColor.YELLOW),
                    )
                }
            }

            return 1
        }
    }
}
