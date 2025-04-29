package com.pokeskies.skiescrates.data.animations.items

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.config.SoundOption

class SpinningItem(
    val preset: String, // The preset that this reward item will use
    val mode: SpinMode, // The mode that this reward item will use to spin
    val slots: List<Int>, // The slots that will be spun, depending on the mode
    @SerializedName("spin_count")
    val spinCount: Int, // How many times the item spins before stopping
    @SerializedName("spin_interval")
    val spinInterval: Int, // The amount of ticks between each spin
    @SerializedName("start_delay")
    val startDelay: Int, // The amount of ticks before the first spin
    @SerializedName("change_interval")
    val changeInterval: Int, // The amount of spins between each time the spinInterval is changed
    @SerializedName("change_amount")
    val changeAmount: Int, // The amount to change spinInterval by
    val sound: SoundOption?, // The sound to play when the item spins
)
