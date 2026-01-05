package com.pokeskies.skiescrates.integrations.holodisplays

import com.pokeskies.skiescrates.data.CrateInstance
import java.util.*

class CrateHologramData(
    val instance: CrateInstance,
    val hiddenPlayers: MutableList<UUID> = mutableListOf()
)