package com.pokeskies.skiescrates.config

class FailureOptions(
    val force: Double? = null,
    val sound: SoundOption? = null,
) {
    override fun toString(): String {
        return "FailureOptions(force=$force, sound=$sound)"
    }
}
