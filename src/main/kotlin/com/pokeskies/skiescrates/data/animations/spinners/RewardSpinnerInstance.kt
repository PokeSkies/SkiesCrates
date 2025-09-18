package com.pokeskies.skiescrates.data.animations.spinners

import com.pokeskies.skiescrates.data.Crate
import com.pokeskies.skiescrates.data.animations.items.SpinMode
import com.pokeskies.skiescrates.data.animations.items.SpinningItem
import com.pokeskies.skiescrates.data.rewards.Reward
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.gui.CrateInventory
import com.pokeskies.skiescrates.utils.RandomCollection
import net.minecraft.server.level.ServerPlayer

class RewardSpinnerInstance(
    spinningItem: SpinningItem,
    isCompleted: Boolean, // Indicates if this Spinner is finished
    isStarted: Boolean, // Indicates if this Spinner has started. If not, ticks is the time until start
    spinsRemaining: Int, // The number of spins remaining before finishing
    ticksPerSpin: Int, // Once spun, this is the number of ticks until the next spin
    ticks: Int, // The number of ticks until the next spin OR until the animation starts
    ticksUntilChange: Int, // The number of spins until the ticksPerSpin is changed
    rewards: MutableMap<Int, Pair<String, Reward>>, // The rewards displayed in the current slots
    private val randomBag: RandomCollection<Pair<String, Reward>>, // The current random bag being used to generate rewards
    private val winSlots: List<Int>, // The slots that will be given to the player as rewards
): ISpinner<Pair<String, Reward>>(spinningItem, isCompleted, isStarted, spinsRemaining, ticksPerSpin, ticks, ticksUntilChange, rewards) {
    constructor(
        spinningItem: SpinningItem,
        rewardBag: RandomCollection<Pair<String, Reward>>,
        winSlots: List<Int>,
    ) : this(
        spinningItem,
        false,
        false,
        spinningItem.spinCount,
        spinningItem.spinInterval,
        ticks = if (spinningItem.startDelay > 0) spinningItem.startDelay else spinningItem.spinInterval,
        spinningItem.changeInterval,
        mutableMapOf(),
        rewardBag,
        winSlots
    )

    override fun generateItem(): Pair<String, Reward>? {
        if (randomBag.size() <= 0) return null
        return randomBag.next()
    }

    override fun updateSlot(gui: CrateInventory, slot: Int, value: Pair<String, Reward>) {
        gui.updateRewardSlot(slot, value)
    }

    fun getFinalRewards(): List<Pair<String, Reward>> {
        return when (spinningItem.mode) {
            SpinMode.RANDOM, SpinMode.SEQUENTIAL, SpinMode.INDEPENDENT -> {
                val winIndexes = winSlots.map { spinningItem.slots.indexOf(it) }.filter { it >= 0 }

                val winRewards = winIndexes.map { index -> pregeneratedSlots[(pregeneratedSlots.size - 1) - index] }

                winRewards
            }
            SpinMode.SYNCED -> {
                val reward = pregeneratedSlots.lastOrNull() ?: return emptyList()
                List(winSlots.size) { reward }
            }
        }
    }

    fun giveRewards(player: ServerPlayer, crate: Crate) {
        getFinalRewards().forEach { (id, reward) ->
            reward.giveReward(player, crate)
        }
    }

    // Check if any rewards cause limit exceptions, remove and regenerate them if so
    // Return null if the list couldn't be regenerated (i.e. all rewards hit their limits)
    // Return the modified bag if successful
    fun validateRewards(crate: Crate, userData: UserData): RandomCollection<Pair<String, Reward>>? {
        val rewards = getFinalRewards()
        for ((i, reward) in rewards.withIndex()) {
            val limit = reward.second.getPlayerLimit()
            if (limit > 0) {
                // If the player can't receive this reward, remove it from the bag and regenerate the slot
                if (!reward.second.canReceive(userData, crate)) {
                    randomBag.remove(reward)
                    if (randomBag.size() <= 0) {
                        return null
                    }
                    val index = when (spinningItem.mode) {
                        SpinMode.RANDOM, SpinMode.SEQUENTIAL, SpinMode.INDEPENDENT -> {
                            (pregeneratedSlots.size - 1) - spinningItem.slots.indexOf(winSlots[i])
                        }
                        SpinMode.SYNCED -> pregeneratedSlots.size - 1
                    }

                    val newReward = generateItem() ?: return null
                    pregeneratedSlots[index] = newReward
                } else {
                    // If the user can receive it, increment their uses and check if we have hit the limit
                    val uses = userData.getRewardLimits(crate.id, reward.first)
                    userData.addRewardUse(crate.id, reward.first)
                    if ((uses + 1) > limit) {
                        randomBag.remove(reward)
                        if (randomBag.size() <= 0) {
                            return null
                        }
                    }
                }
            }
        }

        return randomBag
    }
}
