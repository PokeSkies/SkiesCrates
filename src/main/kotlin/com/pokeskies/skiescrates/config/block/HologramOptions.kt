package com.pokeskies.skiescrates.config.block

import com.google.gson.annotations.SerializedName
import net.minecraft.world.entity.Display.BillboardConstraints

class HologramOptions(
    val text: List<String> = emptyList(),
    val offset: XYZOption = XYZOption(0f, 1f, 0f),
    val scale: XYZOption = XYZOption(1.0f, 1.0f, 1.0f),
    val rotation: XYZOption = XYZOption(0f, 0f, 0f),
    val billboard: BillboardConstraints = BillboardConstraints.CENTER,
    val background: BackgroundOptions? = BackgroundOptions(),
    val shadow: Boolean = true,
    val opacity: Float = 1.0f,
    @SerializedName("update_rate")
    val updateRate: Int = 100,
    @SerializedName("view_distance")
    val viewDistance: Double = 50.0
) {
    class XYZOption(
        val x: Float = 0f,
        val y: Float = 0f,
        val z: Float = 0f
    ) {
        override fun toString(): String {
            return "XYZFloat(x=$x, y=$y, z=$z)"
        }
    }

    class BackgroundOptions(
        val color: String = "000000",
        val opacity: Int = 50,
    ) {
        override fun toString(): String {
            return "BackgroundOptions(color=$color, opacity=$opacity)"
        }
    }

    override fun toString(): String {
        return "HologramOptions(text=$text, offset=$offset, scale=$scale, rotation=$rotation, billboard=$billboard, " +
                "background=$background, shadow=$shadow, opacity=$opacity, updateRate=$updateRate, viewDistance=$viewDistance)"
    }
}
