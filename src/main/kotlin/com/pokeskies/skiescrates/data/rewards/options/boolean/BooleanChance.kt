package com.pokeskies.skiescrates.data.rewards.options.boolean

class BooleanChance(
    val chance: Float
): BooleanOption {
    override fun getValue(): Boolean {
        return Math.random() < chance
    }
}