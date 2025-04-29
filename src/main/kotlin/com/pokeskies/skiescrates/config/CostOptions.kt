package com.pokeskies.skiescrates.config

import com.pokeskies.skiescrates.economy.EconomyType

class CostOptions(
    val provider: EconomyType? = null,
    val currency: String = "",
    val amount: Double = 0.0,
) {
    override fun toString(): String {
        return "CostOptions(provider='$provider', currency='$currency', amount=$amount)"
    }
}
