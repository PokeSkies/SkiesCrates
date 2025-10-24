package com.pokeskies.skiescrates.integrations

import net.fabricmc.loader.api.FabricLoader

enum class ModIntegrations(val modId: String, val clazz: Class<out IntegratedMod>) {
    HOLODISPLAYS("holodisplays", HoloDisplaysIntegration::class.java),
    FLAN("flan", FlanIntegration::class.java);

    fun isModLoaded(): Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    companion object {
        val enabledIntegrations: List<IntegratedMod> by lazy {
            entries.filter { it.isModLoaded() }.mapNotNull {
                try {
                    it.clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}