package com.pokeskies.skiescrates.data.actions

import com.pokeskies.skiescrates.data.actions.types.*

enum class ActionType(val identifier: String, val clazz: Class<*>) {
    COMMAND_CONSOLE("command_console", CommandConsole::class.java),
    COMMAND_PLAYER("command_player", CommandPlayer::class.java),
    MESSAGE("message", MessagePlayer::class.java),
    BROADCAST("broadcast", MessageBroadcast::class.java),
    PLAYSOUND("playsound", PlaySound::class.java);

    companion object {
        fun valueOfAnyCase(name: String): ActionType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
