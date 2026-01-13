package com.pokeskies.skiescrates.config.block

import com.google.gson.annotations.SerializedName

class BlockOptions(
    val locations: MutableList<CrateBlockLocation> = mutableListOf(),
    val model: ModelOptions? = null,
    val hologram: HologramOptions? = null,
    @SerializedName("particle", alternate = ["particles"])
    val particle: String? = null,
) {
    override fun toString(): String {
        return "BlockOptions(locations=$locations, hologram=$hologram, particle=$particle)"
    }
}
