package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.opening.OpeningAnimation
import com.pokeskies.skiescrates.data.opening.OpeningInstance
import com.pokeskies.skiescrates.utils.Utils
import java.util.*

object OpeningManager {
    private val activeInstances: MutableMap<UUID, OpeningInstance> = mutableMapOf()
    private val animations: MutableMap<String, OpeningAnimation> = mutableMapOf()

    fun load() {
        animations.clear()
        ConfigManager.OPENINGS_INVENTORY.forEach { (id, animation) ->
            registerAnimation(id, animation)
        }
        ConfigManager.OPENINGS_WORLD.forEach { (id, animation) ->
            registerAnimation(id, animation)
        }

        Utils.printInfo("Registered ${animations.size} opening animations!")
    }

    fun tick() {
        val iterator = activeInstances.entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            next.value.tick()
        }
    }

    fun addInstance(playerId: UUID, instance: OpeningInstance) {
        activeInstances[playerId] = instance
    }

    fun getInstance(playerId: UUID): OpeningInstance? {
        return activeInstances[playerId]
    }

    fun removeInstance(playerId: UUID) {
        activeInstances.remove(playerId)
    }

    fun registerAnimation(id: String, animation: OpeningAnimation) {
        if (animations.containsKey(id)) {
            Utils.printError("Duplicate opening animation ID found: $id. Skipping...")
            return
        }
        animations[id] = animation
    }

    fun getAnimation(id: String): OpeningAnimation? {
        return animations[id]
    }
}