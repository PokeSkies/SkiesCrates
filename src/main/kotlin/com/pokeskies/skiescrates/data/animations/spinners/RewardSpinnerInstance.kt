package com.pokeskies.skiescrates.data.animations.spinners

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.animations.items.SpinningItem
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.gui.CrateInventory
import net.minecraft.server.level.ServerPlayer

class RewardSpinnerInstance(
    spinningItem: SpinningItem,
    isCompleted: Boolean, // Indicates if this Spinner is finished
    isStarted: Boolean, // Indicates if this Spinner has started. If not, ticks is the time until start
    spinsRemaining: Int, // The number of spins remaining before finishing
    ticksPerSpin: Int, // Once spun, this is the number of ticks until the next spin
    ticks: Int, // The number of ticks until the next spin OR until the animation starts
    ticksUntilChange: Int, // The number of spins until the ticksPerSpin is changed
    private var rewards: MutableMap<Int, Pair<String, Reward>> // The rewards displayed in the current slots
): ISpinner<Pair<String, Reward>>(spinningItem, isCompleted, isStarted, spinsRemaining, ticksPerSpin, ticks, ticksUntilChange, rewards) {
    constructor(
        spinningItem: SpinningItem,
    ) : this(
        spinningItem,
        false,
        false,
        spinningItem.spinCount,
        spinningItem.spinInterval,
        ticks = if (spinningItem.startDelay > 0) spinningItem.startDelay else spinningItem.spinInterval,
        spinningItem.changeInterval,
        mutableMapOf()
    )

    override fun generateItem(player: ServerPlayer, gui: CrateInventory): Pair<String, Reward> {
        return gui.crate.generateReward(player)
    }

    override fun updateSlot(gui: CrateInventory, slot: Int, value: Pair<String, Reward>) {
        gui.updateRewardSlot(slot, value)
    }

    fun giveRewards(player: ServerPlayer, slots: List<Int>, crate: Crate) {
        slots.forEach { slot ->
            rewards[slot]?.let { (id, reward) ->
                reward.giveReward(player, crate)
            }
        }
    }
}
