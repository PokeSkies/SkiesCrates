package com.pokeskies.skiescrates.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.block.CrateBlockLocation
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

class SetCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("set")
            .requires(Permissions.require("${SkiesCrates.MOD_ID}.command.set", 2))
            .then(Commands.argument("crate", StringArgumentType.string())
                .suggests { context, builder ->
                    ConfigManager.CRATES.forEach { builder.suggest(it.key) }
                    builder.buildFuture()
                }
                .executes { ctx: CommandContext<CommandSourceStack> ->
                    execute(
                        ctx,
                        StringArgumentType.getString(ctx, "crate")
                    )
                }
            )
            .build()
    }

    companion object {
        fun execute(
            ctx: CommandContext<CommandSourceStack>,
            crateId: String,
        ): Int {
            val player = ctx.source.playerOrException

            val crate = ConfigManager.CRATES[crateId] ?: run {
                ctx.source.sendMessage(Component.text("Crate $crateId could not be found!", NamedTextColor.RED))
                return 0
            }

            val blockResult = player.pick(5.0, 1.0F, false)
            if (blockResult == null || blockResult !is BlockHitResult) {
                ctx.source.sendMessage(Component.text("You must be looking at a block to set a crate!", NamedTextColor.RED))
                return 0
            }

            val state = player.serverLevel().getBlockState(blockResult.blockPos)

            if (state.isAir) {
                ctx.source.sendMessage(Component.text("You must be looking at a valid block to set a crate!", NamedTextColor.RED))
                return 0
            }

            val dimLoc = DimensionalBlockPos(
                player.serverLevel().dimension().location().asString(),
                blockResult.blockPos.x,
                blockResult.blockPos.y,
                blockResult.blockPos.z
            )
            if (CratesManager.instances.any { (pos, _) -> pos == dimLoc }) {
                ctx.source.sendMessage(TextUtils.toComponent("<red>This block is already set as a ${crate.name} crate!"))
                return 0
            }

            val blockLocation = CrateBlockLocation(dimLoc.dimension, dimLoc.x, dimLoc.y, dimLoc.z)
            crate.block.locations.add(blockLocation)

            if (!ConfigManager.saveFile("crates/${crateId}.json", crate)) {
                ctx.source.sendMessage(Component.text("Failed to save crate data! Check the console for additional errors...", NamedTextColor.RED))
                return 0
            }

            val instance = CratesManager.loadCrateLocation(crate, blockLocation) ?: run {
                ctx.source.sendMessage(Component.text("Failed to load crate location! Check the console for additional errors...", NamedTextColor.RED))
                return 0
            }

            if (FabricLoader.getInstance().isModLoaded("holodisplays")) {
                HologramsManager.loadCrateHologram(dimLoc, instance)
            }

            ctx.source.sendMessage(Component.text("Successfully set a ${crate.name} crate at the position ${dimLoc.x}, ${dimLoc.y}, ${dimLoc.z}!", NamedTextColor.GREEN))

            return 1
        }
    }
}
