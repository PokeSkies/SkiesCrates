package com.pokeskies.skiescrates.data.opening.world

import com.pokeskies.skiescrates.data.opening.world.types.SimpleRollWorldAnimation

enum class WorldAnimationType(val identifier: String, val clazz: Class<*>) {
    SIMPLE_ROLL("simple_roll", SimpleRollWorldAnimation::class.java);

    companion object {
        fun valueOfAnyCase(name: String): WorldAnimationType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}