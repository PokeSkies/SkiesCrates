package com.pokeskies.skiescrates.economy

import com.pokeskies.skiescrates.economy.services.BEconomyService
import com.pokeskies.skiescrates.economy.services.CobbleDollarsEconomyService
import com.pokeskies.skiescrates.economy.services.ImpactorEconomyService
import com.pokeskies.skiescrates.economy.services.PebblesEconomyService
import net.fabricmc.loader.api.FabricLoader

enum class InternalEconomyTypes(
    val identifier: String,
    val modId: String,
    val clazz: Class<out IEconomyService>
) {
    IMPACTOR("impactor", "impactor", ImpactorEconomyService::class.java),
    PEBBLES("pebbles", "pebbles-economy", PebblesEconomyService::class.java),
    COBBLEDOLLARS("cobbledollars", "cobbledollars", CobbleDollarsEconomyService::class.java),
    BECONOMY("beconomy", "beconomy", BEconomyService::class.java);

    fun isModPresent() : Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }
}
