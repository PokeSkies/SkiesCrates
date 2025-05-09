package com.pokeskies.skiescrates.config

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiescrates.utils.FlexibleListAdaptorFactory

class LoggingOptions(
    val enabled: Boolean = true,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val modes: List<LogMode> = listOf(LogMode.CONSOLE),
) {
    enum class LogMode {
        CONSOLE, // Logs all interactions to the console
        FILE, // Logs all interactions to a file/folder based storage
        STORAGE // Logs
    }
}
