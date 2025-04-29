package com.pokeskies.skiescrates.config.block

class HologramOptions(
    val lines: List<String> = emptyList(),
    val offset: OffsetOptions = OffsetOptions(),
    val gap: Double = 0.5,
    val scale: Double = 1.0,
) {
    class OffsetOptions(
        val x: Double = 0.0,
        val y: Double = 0.0,
        val z: Double = 0.0
    ) {
        override fun toString(): String {
            return "OffsetOptions(x=$x, y=$y, z=$z)"
        }
    }

    override fun toString(): String {
        return "HologramOptions(lines=$lines, offset=$offset, gap=$gap, scale=$scale)"
    }
}
