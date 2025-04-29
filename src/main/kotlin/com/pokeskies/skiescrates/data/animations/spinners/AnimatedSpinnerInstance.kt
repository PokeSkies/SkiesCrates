package com.pokeskies.skiescrates.data.animations.spinners

import com.pokeskies.skiescrates.data.animations.items.SpinningItem
import com.pokeskies.skiescrates.gui.CrateInventory
import com.pokeskies.skiescrates.utils.RandomCollection
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class AnimatedSpinnerInstance(
    spinningItem: SpinningItem,
    isCompleted: Boolean, // Indicates if this Spinner is finished
    isStarted: Boolean, // Indicates if this Spinner has started. If not, ticks is the time until start
    spinsRemaining: Int, // The number of spins remaining before finishing
    ticksPerSpin: Int, // Once spun, this is the number of ticks until the next spin
    ticks: Int, // The number of ticks until the next spin OR until the animation starts
    ticksUntilChange: Int, // The number of spins until the ticksPerSpin is changed
    private var items: MutableMap<Int, ItemStack>, // The items displayed in the current slots
    private val randomBag: RandomCollection<ItemStack>,
): ISpinner<ItemStack>(spinningItem, isCompleted, isStarted, spinsRemaining, ticksPerSpin, ticks, ticksUntilChange, items) {
    constructor(
        spinningItem: SpinningItem,
        randomBag: RandomCollection<ItemStack>,
    ) : this(
        spinningItem,
        false,
        false,
        spinningItem.spinCount,
        spinningItem.spinInterval,
        ticks = if (spinningItem.startDelay > 0) spinningItem.startDelay else spinningItem.spinInterval,
        spinningItem.changeInterval,
        mutableMapOf(),
        randomBag,
    )

    override fun generateItem(player: ServerPlayer, gui: CrateInventory): ItemStack {
        return randomBag.next()
    }

    override fun updateSlot(gui: CrateInventory, slot: Int, value: ItemStack) {
        if (slot > gui.size || slot < 0) return
        gui.setSlot(slot, value)
    }
}
