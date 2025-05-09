package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.logging.RewardLog
import java.util.UUID

object LoggingManager {
    fun init() {

    }

    fun logCrateOpen(player: UUID, crateId: String, rewardId: String) {
        if (!ConfigManager.CONFIG.logging.enabled) return
        println("Logging crate open")
        RewardLog(
            player,
            System.currentTimeMillis(),
            crateId,
            rewardId
        ).write()
    }
}
