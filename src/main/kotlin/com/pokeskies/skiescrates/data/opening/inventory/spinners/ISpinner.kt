package com.pokeskies.skiescrates.data.opening.inventory.spinners

import com.pokeskies.skiescrates.data.opening.inventory.items.SpinMode
import com.pokeskies.skiescrates.data.opening.inventory.items.SpinningItem
import com.pokeskies.skiescrates.gui.CrateInventory
import net.minecraft.server.level.ServerPlayer
import kotlin.random.Random

abstract class ISpinner<T>(
    protected val spinningItem: SpinningItem,
    private var isCompleted: Boolean, // Indicates if this Spinner is finished
    private var isStarted: Boolean, // Indicates if this Spinner has started. If not, ticks is the time until start
    private var spinsRemaining: Int, // The number of spins remaining before finishing
    private var ticksPerSpin: Int, // Once spun, this is the number of ticks until the next spin
    private var ticks: Int, // The number of ticks until the next spin OR until the animation starts
    private var ticksUntilChange: Int, // The number of spins until the ticksPerSpin is changed
    private var slots: MutableMap<Int, T> // Map of the currently displayed things in the slots
) {
    protected lateinit var pregeneratedSlots: MutableList<T> // The pregenerated items to be used when spinning to allow for canceling
    private var currentIndex = 0

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

    // This method pregenerates all the items that will be used in the spin depending on the mode
    fun pregenerate() {
        pregeneratedSlots = when (spinningItem.mode) {
            // Since each slot ticks independently, we add all the spins for all slots
            SpinMode.INDEPENDENT -> List(spinningItem.slots.size * spinningItem.spinCount) { generateItem() }.filterNotNull().toMutableList()

            // Each slot has its own sequence of items but all of them are related, so we only generate spinCount items
            SpinMode.SEQUENTIAL, SpinMode.SYNCED -> List(spinningItem.spinCount) { generateItem() }.filterNotNull().toMutableList()

            // Every slot ticks independently, but each spin changes one random slot, so we need to generate a lot of items
            SpinMode.RANDOM -> {
                val list: MutableList<T> = mutableListOf()

                val tempList: MutableList<T> =  List(spinningItem.slots.size) { generateItem() }
                    .filterNotNull()
                    .toMutableList()
                // For every spin, modify one value of $tempList randomly and add all values to the final list
                for (i in 0..<spinningItem.spinCount) {
                    generateItem()?.let {
                        val slot = Random.nextInt(spinningItem.slots.size)
                        tempList[slot] = it
                        list.addAll(tempList)
                    }
                }

                list
            }
        }
    }

    // This method swaps the items in the slots depending on the mode
    private fun spin(player: ServerPlayer, gui: CrateInventory) {
        when (spinningItem.mode) {
            SpinMode.INDEPENDENT, SpinMode.RANDOM -> {
                // These spin the same as the pregenerated slot list is already completely filled out for each slot
                spinningItem.slots.forEach { slot ->
                    val value = pregeneratedSlots[currentIndex++]
                    slots[slot] = value
                    updateSlot(gui, slot, value)
                }
            }
            SpinMode.SEQUENTIAL -> {
                // Individual item sequence is pregenerated, so each time we just need to shift left by one and add a new to the end
                for (i in spinningItem.slots.size - 1 downTo 1) {
                    val slot = spinningItem.slots[i]
                    val tempReward = slots[spinningItem.slots[i - 1]] ?: continue
                    slots[slot] = tempReward
                    updateSlot(gui, slot, tempReward)
                }
                val value = pregeneratedSlots[currentIndex++]
                val slot = spinningItem.slots.first()
                slots[slot] = value
                updateSlot(gui, slot, value)
            }
            SpinMode.SYNCED -> {
                // All items spin the same, so just get the next pregenerated item and set all slots to it
                val value = pregeneratedSlots[currentIndex++]
                spinningItem.slots.forEach { slot ->
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
    abstract fun generateItem(): T?
}
