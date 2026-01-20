package com.pokeskies.skiescrates.data

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.block.HologramOptions
import com.pokeskies.skiescrates.config.block.ModelOptions
import com.pokeskies.skiescrates.data.particles.ParticleAnimation
import com.pokeskies.skiescrates.data.particles.ParticleAnimationOptions
import com.pokeskies.skiescrates.integrations.ModIntegration
import com.pokeskies.skiescrates.integrations.bil.BILCrateData
import com.pokeskies.skiescrates.managers.HologramsManager
import com.pokeskies.skiescrates.utils.Utils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.chunk.LevelChunk

class CrateInstance(
    val crate: Crate,
    val level: ServerLevel,
    val pos: BlockPos,
    val dimPos: DimensionalBlockPos,
    val model: ModelOptions? = null,
    val hologram: HologramOptions? = null,
    val particles: ParticleAnimationOptions? = null,
) {
    private var particleAnimation: ParticleAnimation? = null
    var bilData: BILCrateData? = null

    private var nearPlayers = mutableSetOf<ServerPlayer>()
    private var ticks = 0

    init {
        particleAnimation = particles?.generateAnimation(this)

        model?.let { modelOptions ->
            if (!ModIntegration.BIL.isModLoaded()) {
                Utils.printError("The crate '${crate.id}' is using a crate model but the Blockbench Import Library mod is not installed!")
                return@let
            }

            var chunk: LevelChunk? = null
            if (level.isLoaded(pos)) {
                try {
                    chunk = level.getChunkAt(pos)
                } catch (e: Exception) {
                    Utils.printError("Error while attaching BIL Crate at chunk ${pos}!")
                    e.printStackTrace()
                }
            }

            bilData = BILCrateData.create(this, chunk, modelOptions)
        }
    }

    fun destroy() {
        bilData?.holder?.destroy()

        if (FabricLoader.getInstance().isModLoaded("holodisplays")) {
            HologramsManager.unloadCrateHologram(this)
        }
    }

    fun tick() {
        particleAnimation?.let { particle ->
            SkiesCrates.INSTANCE.runOnParticleThread {
                if (ticks++ >= 20) {
                    ticks = 0
                    nearPlayers = level.players().filter { p ->
                        p.distanceToSqr(pos.bottomCenter) < particle.getDistance()
                    }.toMutableSet()
                }

                particle.tick(nearPlayers.toList())
            }
        }
    }
}