package com.pokeskies.skiescrates.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class ReloadCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("reload")
            .requires(Permissions.require("${SkiesCrates.MOD_ID}.command.reload", 2))
            .executes(Companion::reload)
            .build()
    }

    companion object {
        fun reload(ctx: CommandContext<CommandSourceStack>): Int {
            SkiesCrates.INSTANCE.reload()
            ctx.source.sendMessage(Component.text("Reloaded ${SkiesCrates.MOD_NAME}!").color(NamedTextColor.GREEN))
            return 1
        }
    }
}
