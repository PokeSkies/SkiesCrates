package com.pokeskies.skiescrates.data.logging;

import com.pokeskies.skiescrates.config.ConfigManager;
import com.pokeskies.skiescrates.config.LoggingOptions;

abstract class LogEntry {
    public void write() {
        for (LoggingOptions.LogMode mode : ConfigManager.CONFIG.getLogging().getModes()) {
            switch (mode) {
                case CONSOLE -> logToConsole();
                case FILE -> logToFile();
                case STORAGE -> logToStorage();
            }
        }
    }

    abstract void logToConsole();
    abstract void logToFile();
    abstract void logToStorage();
}
