package com.pokeskies.skiescrates.data.rewards

import com.pokeskies.skiescrates.data.rewards.types.CommandConsole
import com.pokeskies.skiescrates.data.rewards.types.CommandPlayer

enum class RewardType(val identifier: String, val clazz: Class<*>) {
    COMMAND_CONSOLE("command_console", CommandConsole::class.java),
    COMMAND_PLAYER("command_player", CommandPlayer::class.java);

    companion object {
        fun valueOfAnyCase(name: String): RewardType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
