package com.pokeskies.skiescrates.utils

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.CrateOpenData
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.events.CrateOpenedEvent
import kotlinx.coroutines.launch
import net.minecraft.server.level.ServerPlayer

object WebhookUtils {
    fun registerEvents() {
        CrateOpenedEvent.EVENT.register { player, crate, openData, rewards ->
            SkiesCrates.asyncScope.launch {
                sendCrateOpened(player, crate, openData, rewards)
            }
        }
    }

    fun sendKeyAlert(player: ServerPlayer, message: String) {
        try {
            ConfigManager.CONFIG.webhooks.duplicateKey.send(playerPlaceholders(player) + ("%alert_message%" to message))
        } catch (exception: Exception) {
            Utils.printError("Failed to send duplicate key webhook for ${player.name.string}: ${exception.message}")
        }
    }

    fun sendCrateOpened(
        player: ServerPlayer,
        crate: Crate,
        openData: CrateOpenData,
        rewards: List<Reward>,
    ) {
        try {
            ConfigManager.CONFIG.webhooks.crateOpened.send(crateOpenPlaceholders(player, crate, openData, rewards))
        } catch (exception: Exception) {
            Utils.printError("Failed to send crate open webhook for ${player.name.string} opening crate ${crate.id}: ${exception.message}")
        }
    }

    private fun crateOpenPlaceholders(
        player: ServerPlayer,
        crate: Crate,
        openData: CrateOpenData,
        rewards: List<Reward>,
    ): Map<String, String> {
        return playerPlaceholders(player) + mapOf(
            "%crate_id%" to crate.id,
            "%crate_name%" to crate.name.ifEmpty { crate.id }.asNative().string,
            "%rewards%" to formatRewards(rewards),
            "%reward_count%" to rewards.size.toString(),
            "%reward_names%" to rewards.joinToString(", ") { it.name.ifEmpty { it.id }.asNative().string },
            "%reward_ids%" to rewards.joinToString(", ") { it.id },
        )
    }

    private fun playerPlaceholders(player: ServerPlayer): Map<String, String> {
        return mapOf(
            "%player%" to player.name.string,
            "%player_uuid%" to player.stringUUID
        )
    }

    private fun formatRewards(rewards: List<Reward>): String {
        if (rewards.isEmpty()) return "None"

        val rewardText = rewards.joinToString("\n") { reward ->
            "- ${reward.name.ifEmpty { reward.id }.asNative().string}"
        }

        return rewardText
    }
}
