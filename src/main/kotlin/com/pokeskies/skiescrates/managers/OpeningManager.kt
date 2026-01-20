package com.pokeskies.skiescrates.managers

import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.opening.OpeningAnimation
import com.pokeskies.skiescrates.data.opening.OpeningInstance
import com.pokeskies.skiescrates.utils.Utils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object OpeningManager {
    private val activeInstances: MutableMap<UUID, OpeningInstance> = ConcurrentHashMap()
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
        for ((_, instance) in activeInstances) {
            instance.tick()
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