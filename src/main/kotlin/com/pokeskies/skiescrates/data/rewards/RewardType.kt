package com.pokeskies.skiescrates.data.rewards

import com.pokeskies.skiescrates.data.rewards.types.CommandConsoleReward
import com.pokeskies.skiescrates.data.rewards.types.CommandPlayerReward
import com.pokeskies.skiescrates.data.rewards.types.ItemReward

enum class RewardType(val identifier: String, val clazz: Class<*>) {
    COMMAND_CONSOLE("command_console", CommandConsoleReward::class.java),
    COMMAND_PLAYER("command_player", CommandPlayerReward::class.java),
    ITEM("item", ItemReward::class.java);

    companion object {
        fun valueOfAnyCase(name: String): RewardType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
