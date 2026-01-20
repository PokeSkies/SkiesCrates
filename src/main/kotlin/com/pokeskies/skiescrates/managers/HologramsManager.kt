package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.data.CrateInstance
import com.pokeskies.skiescrates.integrations.holodisplays.CrateHologramData
import com.pokeskies.skiescrates.mixins.ViewerHandlerAccessor
import com.pokeskies.skiescrates.utils.Utils
import dev.furq.holodisplays.api.HoloDisplaysAPI
import dev.furq.holodisplays.api.HoloDisplaysAPI.HologramBuilder
import dev.furq.holodisplays.handlers.ViewerHandler
import net.minecraft.server.level.ServerPlayer

object HologramsManager {
    private var hologramsAPI: HoloDisplaysAPI = HoloDisplaysAPI.get(SkiesCrates.MOD_ID)
    private val holograms = mutableMapOf<String, CrateHologramData>()

    fun load() {
        unload()
        CratesManager.getAllInstances().forEach { instance ->
            loadCrateHologram(instance)
        }
    }

    fun loadCrateHologram(instance: CrateInstance) {
        val hologramConfig = instance.hologram ?: return
        val id = SkiesCrates.asResource(instance.dimPos.hashCode().toString()).toString()

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
                instance.dimPos.x + 0.5f + hologramConfig.offset.x,
                instance.dimPos.y + 0.5f + hologramConfig.offset.y,
                instance.dimPos.z + 0.5f + hologramConfig.offset.z
            )
            .world(instance.dimPos.dimension)
            .addDisplay(id)
            .updateRate(hologramConfig.updateRate)
            .viewRange(hologramConfig.viewDistance)

        if (!hologramsAPI.registerHologram(id, builder.build())) {
            Utils.printError("Failed to register hologram with ID: $id")
            return
        }
        holograms[id] = CrateHologramData(instance)
    }

    fun unload() {
        holograms.forEach { (id, _) ->
            hologramsAPI.unregisterDisplay(id)
            hologramsAPI.unregisterHologram(id)
        }
    }

    fun unloadCrateHologram(instance: CrateInstance) {
        val id = SkiesCrates.asResource(instance.dimPos.hashCode().toString()).toString()
        hologramsAPI.unregisterDisplay(id)
        hologramsAPI.unregisterHologram(id)
        holograms.remove(id)
    }

    fun getHologramData(id: String): CrateHologramData? {
        return holograms[id]
    }

    fun hideHologramForPlayer(player: ServerPlayer, crateInstance: CrateInstance) {
        val id = SkiesCrates.asResource(crateInstance.dimPos.hashCode().toString()).toString()
        val crateHologram = holograms[id] ?: return

        crateHologram.hiddenPlayers.add(player.uuid)
        (ViewerHandler as ViewerHandlerAccessor).invokeRemoveViewer(player, id)
    }

    fun showHologramForPlayer(player: ServerPlayer, crateInstance: CrateInstance) {
        val id = SkiesCrates.asResource(crateInstance.dimPos.hashCode().toString()).toString()
        val crateHologram = holograms[id] ?: return

        crateHologram.hiddenPlayers.remove(player.uuid)
    }
}
