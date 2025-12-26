package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.data.CrateInstance
import com.pokeskies.skiescrates.utils.Utils
import dev.furq.holodisplays.api.HoloDisplaysAPI
import dev.furq.holodisplays.api.HoloDisplaysAPI.HologramBuilder

object HologramsManager {
    private var hologramsAPI: HoloDisplaysAPI = HoloDisplaysAPI.get(SkiesCrates.MOD_ID)
    private val holograms = mutableMapOf<String, CrateInstance>()

    fun load() {
        CratesManager.instances.forEach { (location, instance) ->
            val hologramConfig = instance.hologram ?: return@forEach
            val id = SkiesCrates.asResource(location.hashCode().toString()).toString()

            hologramsAPI.createTextDisplay(
                id
            ) { builder ->
                builder.text(*hologramConfig.text.map {
                    instance.crate.parsePlaceholders(it)
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
            holograms[id] = instance
        }
    }

    fun unload() {
        holograms.forEach { (id, crate) ->
            hologramsAPI.unregisterDisplay(id)
            hologramsAPI.unregisterHologram(id)
        }
    }
}
