package com.pokeskies.skiescrates.data.rewards.options.int

class IntValue(
    val int: Int
): IntOption {
    override fun getValue(): Int {
        return int
    }
}