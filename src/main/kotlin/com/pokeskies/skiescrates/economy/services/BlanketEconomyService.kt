package com.pokeskies.skiescrates.economy.services

import com.pokeskies.skiescrates.economy.IEconomyService
import com.pokeskies.skiescrates.utils.Utils
import net.minecraft.server.level.ServerPlayer
import org.blanketeconomy.api.BlanketEconomy
import java.math.BigDecimal

class BlanketEconomyService : IEconomyService {
    init {
        Utils.printInfo("BlanketEconomy has been found and loaded for any Currency actions/requirements!")
    }

    override fun balance(player: ServerPlayer, currency: String) : Double {
        return BlanketEconomy.getAPI().getBalance(player.uuid, currency).toDouble()
    }

    override fun withdraw(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        return BlanketEconomy.getAPI().subtractBalance(player.uuid, BigDecimal.valueOf(amount), currency)
    }

    override fun deposit(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        BlanketEconomy.getAPI().addBalance(player.uuid, BigDecimal.valueOf(amount), currency)
        return true
    }

    override fun set(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        BlanketEconomy.getAPI().setBalance(player.uuid, BigDecimal.valueOf(amount), currency)
        return true
    }
}
