package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.CrateConfig
import com.pokeskies.skiescrates.utils.Utils
import dev.furq.holodisplays.api.HoloDisplaysAPI
import dev.furq.holodisplays.api.HoloDisplaysAPI.HologramBuilder

object HologramsManager {
    private var hologramsAPI: HoloDisplaysAPI = HoloDisplaysAPI.get()
    private val holograms = mutableMapOf<String, CrateConfig>()

    fun load() {
        CratesManager.locations.forEach { (location, crate) ->
            val hologramConfig = crate.block.hologram ?: return@forEach
            val id = SkiesCrates.asResource(location.hashCode().toString()).toString()

            hologramsAPI.createTextDisplay(
                id
            ) { builder ->
                builder.text(*hologramConfig.text.map {
                    crate.parsePlaceholders(it)
                }.toTypedArray())
                builder.scale(hologramConfig.scale.x, hologramConfig.scale.y, hologramConfig.scale.z)
                builder.rotation(hologramConfig.rotation.x, hologramConfig.rotation.y, hologramConfig.rotation.z)
                builder.billboardMode(hologramConfig.billboard.name)
                builder.shadow(hologramConfig.shadow)
                hologramConfig.background?.let {
                    builder.backgroundColor(it.color, it.opacity)
                }
                builder.opacity(hologramConfig.opacity)
            }

            val builder: HologramBuilder = hologramsAPI.createHologramBuilder()
                // Position is modified by 0.5f to center the hologram on the block
                .position(
                    location.x + 0.5f + hologramConfig.offset.x,
                    location.y + 0.5f + hologramConfig.offset.y,
                    location.z + 0.5f + hologramConfig.offset.z
                )
                .world(location.dimension)
                .addDisplay(id)
                .updateRate(hologramConfig.updateRate)
                .viewRange(hologramConfig.viewDistance)

            if (!hologramsAPI.registerHologram(id, builder.build())) {
                Utils.printError("Failed to register hologram with ID: $id")
                return@forEach
            }
            holograms[id] = crate
        }
    }

    fun unload() {
        holograms.forEach { (id, crate) ->
            hologramsAPI.unregisterHologram(id)
        }
    }
}
