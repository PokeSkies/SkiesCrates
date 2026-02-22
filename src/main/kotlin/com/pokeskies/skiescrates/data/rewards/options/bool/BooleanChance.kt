package com.pokeskies.skiescrates.data.rewards.options.bool

class BooleanChance(
    val chance: Float
): BooleanOption {
    override fun getValue(): Boolean {
        return Math.random() < chance
    }
}