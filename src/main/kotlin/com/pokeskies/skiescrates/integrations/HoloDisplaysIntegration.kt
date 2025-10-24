package com.pokeskies.skiescrates.integrations

import com.pokeskies.skiescrates.managers.HologramsManager
import com.pokeskies.skiescrates.utils.Utils

class HoloDisplaysIntegration: IntegratedMod {
    override fun init() {
        Utils.printInfo("The mod HoloDisplays was found, enabling integrations...")
        HologramsManager.load()
    }

    override fun shutdown() {
        Utils.printInfo("Shutting down HoloDisplays integrations...")
        HologramsManager.unload()
    }
}