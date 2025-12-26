package com.pokeskies.skiescrates.config.block

import com.pokeskies.skiescrates.data.DimensionalBlockPos

class CrateBlockLocation(
    val dimension: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val model: ModelOptions? = null, // Per location override
    val hologram: HologramOptions? = null, // Per location override
    val particles: ParticleOptions? = null, // Per location override
) {
    fun getDimensionalBlockPos(): DimensionalBlockPos {
        return DimensionalBlockPos(dimension, x, y, z)
    }
}