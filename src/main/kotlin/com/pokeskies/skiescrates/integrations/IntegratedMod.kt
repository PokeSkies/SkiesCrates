package com.pokeskies.skiescrates.integrations

interface IntegratedMod {
    fun onInit() {}
    fun onServerStarted() {}
    fun onServerStarting() {}
    fun onServerShutdown() {}
}