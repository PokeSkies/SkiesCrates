package com.pokeskies.skiescrates.data.rewards.options.boolean

class BooleanValue(val bool: Boolean) : BooleanOption {
    override fun getValue(): Boolean {
        return bool
    }
}