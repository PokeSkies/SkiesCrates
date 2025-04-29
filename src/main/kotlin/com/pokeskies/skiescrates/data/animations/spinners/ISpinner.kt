package com.pokeskies.skiescrates.data.animations.spinners

import com.pokeskies.skiescrates.data.animations.items.SpinMode
import com.pokeskies.skiescrates.data.animations.items.SpinningItem
import com.pokeskies.skiescrates.gui.CrateInventory
import net.minecraft.server.level.ServerPlayer

abstract class ISpinner<T>(
    private val spinningItem: SpinningItem,
    private var isCompleted: Boolean, // Indicates if this Spinner is finished
    private var isStarted: Boolean, // Indicates if this Spinner has started. If not, ticks is the time until start
    private var spinsRemaining: Int, // The number of spins remaining before finishing
    private var ticksPerSpin: Int, // Once spun, this is the number of ticks until the next spin
    private var ticks: Int, // The number of ticks until the next spin OR until the animation starts
    private var ticksUntilChange: Int, // The number of spins until the ticksPerSpin is changed
    private var slots: MutableMap<Int, T> // Map of the currently displayed things in the slots
) {
    // Ticks the current spinner and returns if the spinner is completed
    fun tick(player: ServerPlayer, gui: CrateInventory) {
        if (isStarted) {
            ticks--
            if (ticks <= 0) {
                ticksUntilChange--
                if (ticksUntilChange <= 0) {
                    ticksUntilChange = spinningItem.changeInterval
                    ticksPerSpin += spinningItem.changeAmount
                }
                ticks = ticksPerSpin
                spinsRemaining--

                spin(player, gui)

                if (spinsRemaining <= 0) {
                    isCompleted = true
                }
            }
        } else {
            ticks--
            if (ticks <= 0) {
                isStarted = true
                ticks = ticksPerSpin

                spin(player, gui)

                if (--spinsRemaining <= 0) {
                    isCompleted = true
                }
            }
        }
    }

    // This method swaps the items in the slots depending on the mode
    private fun spin(player: ServerPlayer, gui: CrateInventory) {
        when (spinningItem.mode) {
            SpinMode.INDEPENDENT -> {
                spinningItem.slots.forEach { slot ->
                    val value = generateItem(player, gui)
                    slots[slot] = value
                    updateSlot(gui, slot, value)
                }
            }
            SpinMode.SEQUENTIAL -> {
                // Shift the entire list left one and add a new to the end
                for (i in spinningItem.slots.size - 1 downTo 1) {
                    val slot = spinningItem.slots[i]
                    val tempReward = slots[spinningItem.slots[i - 1]] ?: continue
                    slots[slot] = tempReward
                    updateSlot(gui, slot, tempReward)
                }
                val value = generateItem(player, gui)
                val slot = spinningItem.slots.first()
                slots[slot] = value
                updateSlot(gui, slot, value)
            }
            SpinMode.SYNCED -> {
                val value = generateItem(player, gui)
                spinningItem.slots.forEach { slot ->
                    slots[slot] = value
                    updateSlot(gui, slot, value)
                }
            }
            SpinMode.RANDOM -> {
                if (!isStarted) {
                    spinningItem.slots.forEach { slot ->
                        val value = generateItem(player, gui)
                        slots[slot] = value
                        updateSlot(gui, slot, value)
                    }
                    return
                }
                spinningItem.slots.randomOrNull()?.let { slot ->
                    val value = generateItem(player, gui)
                    slots[slot] = value
                    updateSlot(gui, slot, value)
                }
            }
        }
        spinningItem.sound?.playSound(player)
    }

    fun isCompleted(): Boolean {
        return isCompleted
    }

    // This is called when a slot in the spinner spins and needs to be updated in the GUI. This
    // lets different spinners do different things
    abstract fun updateSlot(gui: CrateInventory, slot: Int, value: T)

    // Calling this will generate the data necessary to spin a item slot depending on the type of spinner
    abstract fun generateItem(player: ServerPlayer, gui: CrateInventory): T
}
