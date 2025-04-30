package com.pokeskies.skiescrates.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.gui.KeysInventory
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

class KeysCommand {
    private val aliases = ConfigManager.CONFIG.keys.aliases

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires(Permissions.require("${SkiesCrates.MOD_ID}.command.keys", 2))
                .executes { ctx -> execute(ctx, ctx.source.playerOrException) }
                .build()
        }

        rootCommands.forEach { root ->
            dispatcher.root.addChild(root)
        }
    }

    companion object {
        fun execute(ctx: CommandContext<CommandSourceStack>, target: ServerPlayer): Int {
            if (ctx.source.isPlayer && ConfigManager.CONFIG.keys.useMenu) {
                KeysInventory(ctx.source.playerOrException, target).open()
            } else {
                val playerData = SkiesCrates.INSTANCE.storage?.getUser(target.uuid) ?: run {
                    ctx.source.sendSystemMessage(net.minecraft.network.chat.Component.literal("Player data is null!").withStyle(
                        ChatFormatting.RED))
                    return 0
                }

                if (playerData.keys.isEmpty()) {
                    ctx.source.sendSystemMessage(net.minecraft.network.chat.Component.literal("${target.name.string} has no keys.").withStyle(
                        ChatFormatting.RED))
                    return 0
                }

                ctx.source.sendSystemMessage(net.minecraft.network.chat.Component.literal("${target.name.string}'s keys:").withStyle(
                    ChatFormatting.GOLD))
                for ((keyId, amount) in playerData.keys) {
                    val key = ConfigManager.KEYS[keyId]
                    if (key != null) {
                        ctx.source.sendSystemMessage(net.minecraft.network.chat.Component.literal(" - ${key.name} ($keyId): $amount").withStyle(
                            ChatFormatting.GREEN))
                    } else {
                        ctx.source.sendSystemMessage(net.minecraft.network.chat.Component.literal(" - Unknown Key ($keyId): $amount").withStyle(
                            ChatFormatting.RED))
                    }
                }
            }

            return 1
        }
    }
}
