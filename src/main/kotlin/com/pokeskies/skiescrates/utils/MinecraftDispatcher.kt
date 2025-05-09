package com.pokeskies.skiescrates.utils

import kotlinx.coroutines.CoroutineDispatcher
import net.minecraft.server.MinecraftServer
import kotlin.coroutines.CoroutineContext

class MinecraftDispatcher(private val server: MinecraftServer) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (server.isSameThread) {
            block.run()
        } else {
            server.execute(block)
        }
    }
}
