package com.pokeskies.skiescrates.config.block

class BlockOptions(
    val locations: List<CrateBlockLocation> = listOf(),
    val model: ModelOptions? = null,
    val hologram: HologramOptions? = null,
    val particles: ParticleOptions? = null,
) {
    override fun toString(): String {
        return "BlockOptions(locations=$locations, hologram=$hologram, particles=$particles)"
    }
}
