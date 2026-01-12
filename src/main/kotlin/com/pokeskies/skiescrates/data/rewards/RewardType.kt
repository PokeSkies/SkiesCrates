package com.pokeskies.skiescrates.data.rewards

import com.pokeskies.skiescrates.data.rewards.types.CommandConsoleReward
import com.pokeskies.skiescrates.data.rewards.types.CommandPlayerReward
import com.pokeskies.skiescrates.data.rewards.types.ItemReward
import com.pokeskies.skiescrates.data.rewards.types.PokemonReward

enum class RewardType(val identifier: String, val clazz: Class<*>, val dependencies: List<String> = emptyList()) {
    COMMAND_CONSOLE("command_console", CommandConsoleReward::class.java),
    COMMAND_PLAYER("command_player", CommandPlayerReward::class.java),
    ITEM("item", ItemReward::class.java),
    POKEMON("pokemon", PokemonReward::class.java, listOf("cobblemon"));

    companion object {
        fun valueOfAnyCase(name: String): RewardType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
