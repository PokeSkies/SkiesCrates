package com.pokeskies.skiescrates.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.commands.subcommands.DebugCommand
import com.pokeskies.skiescrates.commands.subcommands.GiveCommand
import com.pokeskies.skiescrates.commands.subcommands.KeyCommand
import com.pokeskies.skiescrates.commands.subcommands.ReloadCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class BaseCommand {
    private val aliases = listOf("skiescrates", "crates", "crate")

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires(Permissions.require("${SkiesCrates.MOD_ID}.command.base", 2))
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            ReloadCommand().build(),
            DebugCommand().build(),
            GiveCommand().build(),
            KeyCommand().build()
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }
}
