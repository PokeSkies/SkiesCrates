package com.pokeskies.skiescrates.commands.subcommands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.commands.KeysCommand
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.managers.CratesManager
import com.pokeskies.skiescrates.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

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
            .executes { ctx -> KeysCommand.execute(ctx, ctx.source.playerOrException)}
            .build()
    }

    companion object {
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
                CratesManager.giveKey(key, player, amount, silent)
            }

            val successful = results.filter { it }.size
            when (successful) {
                0 -> ctx.source.sendMessage(
                    Component.text("Failed to give ${amount}x $keyId keys to any players!").color(NamedTextColor.RED)
                )
                targets.size -> ctx.source.sendMessage(
                    Component.text("Successfully gave ${amount}x $keyId keys to $successful players!").color(NamedTextColor.GREEN),
                )
                else -> ctx.source.sendMessage(
                    Component.text("Successfully gave ${amount}x $keyId keys to $successful player(s), " +
                            "but failed to give it to ${targets.size - successful} player(s)!").color(NamedTextColor.YELLOW),
                )
            }

            return 1
        }
    }
}
