package com.pokeskies.skiescrates.config.block

import com.pokeskies.skiescrates.data.DimensionalBlockPos

class BlockOptions(
    val locations: List<DimensionalBlockPos> = listOf(),
    val model: ModelOptions? = null,
    val hologram: HologramOptions? = null,
    val particles: ParticleOptions? = null,
) {
    override fun toString(): String {
        return "BlockOptions(locations=$locations, hologram=$hologram, particles=$particles)"
    }
}
