package com.pokeskies.skiescrates.data.particles

import com.pokeskies.skiescrates.data.CrateInstance
import com.pokeskies.skiescrates.data.particles.effects.ParticleEffect

class ParticleAnimationOptions(
    val mode: AnimationMode,
    val distance: Double,
    val effects: List<ParticleEffect>
) {
    fun generateAnimation(instance: CrateInstance): ParticleAnimation {
        val animation = ParticleAnimation()

        animation.setMode(mode)
        animation.setDistance(distance)

        for (effect in effects) {
            animation.addTimeline(effect.generateTimeline(instance))
        }

        return animation
    }
}