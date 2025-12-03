package com.pokeskies.skiescrates.integrations

import com.pokeskies.skiescrates.managers.HologramsManager
import com.pokeskies.skiescrates.utils.Utils

class HoloDisplaysIntegration: IntegratedMod {
    override fun onServerStarted() {
        Utils.printInfo("The mod HoloDisplays was found, enabling integrations...")
        HologramsManager.load()
    }

    override fun onServerShutdown() {
        Utils.printInfo("Shutting down HoloDisplays integrations...")
        HologramsManager.unload()
    }
}