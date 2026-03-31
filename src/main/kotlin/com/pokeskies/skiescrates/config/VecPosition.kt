package com.pokeskies.skiescrates.config

import net.minecraft.world.phys.Vec3

class VecPosition(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0
) {
    fun toVec3(): Vec3 {
        return Vec3(x, y, z)
    }
}