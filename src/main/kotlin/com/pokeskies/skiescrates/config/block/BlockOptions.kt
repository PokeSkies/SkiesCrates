package com.pokeskies.skiescrates.config.block

class BlockOptions(
    val locations: MutableList<CrateBlockLocation> = mutableListOf(),
    val model: ModelOptions? = null,
    val hologram: HologramOptions? = null,
    val particles: String? = null,
) {
    override fun toString(): String {
        return "BlockOptions(locations=$locations, hologram=$hologram, particles=$particles)"
    }
}
