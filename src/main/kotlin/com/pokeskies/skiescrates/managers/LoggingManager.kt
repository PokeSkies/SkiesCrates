package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.SkiesCrates.Companion.asyncScope
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.logging.CrateLogEntry
import com.pokeskies.skiescrates.storage.IStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CompletableFuture

object LoggingManager {
    fun logCrateRewards(player: UUID, crateId: String, rewardId: String) {
        if (!ConfigManager.CONFIG.logging.enabled) return
        println("Logging crate reward")
        asyncScope.launch {
            CrateLogEntry(
                player,
                System.currentTimeMillis(),
                crateId,
                rewardId
            ).write()
        }
    }
}
