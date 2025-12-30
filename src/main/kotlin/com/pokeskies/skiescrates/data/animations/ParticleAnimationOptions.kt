package com.pokeskies.skiescrates.data.animations

import com.pokeskies.skiescrates.data.CrateInstance
import com.pokeskies.skiescrates.data.animations.particles.AnimationMode
import com.pokeskies.skiescrates.data.animations.particles.ParticleAnimation
import com.pokeskies.skiescrates.data.animations.particles.effects.ParticleEffect

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