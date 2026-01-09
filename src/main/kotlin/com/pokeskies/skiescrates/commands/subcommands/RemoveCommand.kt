package com.pokeskies.skiescrates.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.DimensionalBlockPos
import com.pokeskies.skiescrates.managers.CratesManager
import com.pokeskies.skiescrates.managers.HologramsManager
import com.pokeskies.skiescrates.utils.SubCommand
import com.pokeskies.skiescrates.utils.TextUtils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.world.phys.BlockHitResult

class RemoveCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("remove")
            .requires(Permissions.require("${SkiesCrates.MOD_ID}.command.remove", 2))
            .executes { ctx: CommandContext<CommandSourceStack> ->
                execute(ctx)
            }
            .build()
    }

    companion object {
        fun execute(
            ctx: CommandContext<CommandSourceStack>
        ): Int {
            val player = ctx.source.playerOrException

            val blockResult = player.pick(5.0, 1.0F, false)
            if (blockResult == null || blockResult !is BlockHitResult) {
                ctx.source.sendMessage(Component.text("You must be looking at a block to remove a crate!", NamedTextColor.RED))
                return 0
            }

            val state = player.serverLevel().getBlockState(blockResult.blockPos)
            if (state.isAir) {
                ctx.source.sendMessage(Component.text("You must be looking at a valid block to remove a crate!", NamedTextColor.RED))
                return 0
            }

            val dimLoc = DimensionalBlockPos(
                player.serverLevel().dimension().location().asString(),
                blockResult.blockPos.x,
                blockResult.blockPos.y,
                blockResult.blockPos.z
            )

            val locations = CratesManager.instances.filter { (pos, _) -> pos == dimLoc }
            if (locations.isEmpty()) {
                ctx.source.sendMessage(TextUtils.toComponent("<red>This block is not set as any active crate!"))
                return 0
            }

            var removed = 0
            for ((pos, instance) in locations) {
                CratesManager.removeCrateLocation(instance) // If it's not removed from here, it was likely never loaded "properly", which is possible
                if (!ConfigManager.saveFile("crates/${instance.crate.id}.json", instance.crate)) {
                    ctx.source.sendMessage(Component.text("Failed to save crate data for ${instance.crate.id}! Check the console for additional errors...", NamedTextColor.RED))
                    continue
                }

                removed++

                if (FabricLoader.getInstance().isModLoaded("holodisplays")) {
                    HologramsManager.unloadCrateHologram(pos)
                }
            }

            if (removed == 0) {
                ctx.source.sendMessage(Component.text("Failed to remove crates at the position ${dimLoc.x}, ${dimLoc.y}, ${dimLoc.z}! Check the console for additional errors...", NamedTextColor.RED))
                return 0
            }

            ctx.source.sendMessage(Component.text("Successfully removed $removed crate(s) at the position ${dimLoc.x}, ${dimLoc.y}, ${dimLoc.z}!", NamedTextColor.GREEN))

            return 1
        }
    }
}
