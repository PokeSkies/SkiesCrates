package com.pokeskies.skiescrates.data

import com.pokeskies.skiescrates.integrations.ModIntegration
import com.pokeskies.skiescrates.integrations.bil.BILIntegration
import com.pokeskies.skiescrates.integrations.bil.VirtualCrateModel
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

class CrateInstance(
    val crate: Crate,
    val level: ServerLevel,
    val pos: BlockPos,
) {
    private var crateModel: VirtualCrateModel? = null

    init {
        crate.config.block.model?.let { modelOptions ->
            if (!ModIntegration.BIL.isModLoaded()) {
                Utils.printError("The crate '${crate.id}' is using a crate model but the Blockbench Import Library mod is not installed!")
                return@let
            }

            val integration = ModIntegration.BIL.getIntegration() as? BILIntegration ?: run {
                Utils.printError("The crate '${crate.id}' is using a model but the Blockbench Import Library integration is not available!")
                return@let
            }

            val model = integration.getModel(modelOptions.id) ?: run {
                Utils.printError("The crate '${crate.id}' is using a model '$${modelOptions.id}' which could not be found!")
                return@let
            }

            crateModel = VirtualCrateModel(model, level, pos.center)
            println("Created crate model for crate '${crate.id}' at $pos is $crateModel")
        }
    }

    fun tick() {

    }
}