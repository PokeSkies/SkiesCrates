package com.pokeskies.skiescrates.data.opening

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.managers.OpeningManager
import net.minecraft.server.level.ServerPlayer

abstract class OpeningInstance(
    val player: ServerPlayer,
    val crate: Crate,
) {
    abstract fun setup()

    abstract fun tick()

    open fun stop() {
        destroy()
    }

    open fun destroy() {
        OpeningManager.removeInstance(player.uuid)
    }
}