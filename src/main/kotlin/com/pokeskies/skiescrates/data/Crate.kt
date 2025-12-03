package com.pokeskies.skiescrates.data

import com.pokeskies.skiescrates.config.CrateConfig
import com.pokeskies.skiescrates.managers.CratesManager.locations
import com.pokeskies.skiescrates.utils.Utils

class Crate(
    val config: CrateConfig
) {
    val id = config.id
    val instances: MutableMap<DimensionalBlockPos, CrateInstance> = mutableMapOf()

    init {
        config.block.locations.forEach { loc ->
            for (location in config.block.locations) {
                val level = Utils.getLevel(location.dimension)
                if (level == null) {
                    Utils.printError("Crate ${config.name} has an invalid dimension location: $location")
                    return@forEach
                }
                if (locations.containsKey(location)) {
                    Utils.printError("Crate ${config.name} has a duplicate location: $location")
                    return@forEach
                }

                if (config.block.particles != null) {
                    // TODO: Particle implementation
                }

                instances[loc] = CrateInstance(this, level,  loc.getBlockPos())
            }
        }
    }

    fun tick() {
        instances.forEach { (_, instance) -> instance.tick() }
    }
}