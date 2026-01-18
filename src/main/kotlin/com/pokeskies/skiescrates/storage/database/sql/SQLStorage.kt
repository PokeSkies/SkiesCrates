package com.pokeskies.skiescrates.storage.database.sql

import com.google.gson.reflect.TypeToken
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.pokeskies.skiescrates.data.userdata.CrateData
import com.pokeskies.skiescrates.data.userdata.UsedKeyData
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.IStorage
import com.pokeskies.skiescrates.storage.StorageType
import com.pokeskies.skiescrates.storage.database.sql.providers.MySQLProvider
import com.pokeskies.skiescrates.storage.database.sql.providers.SQLiteProvider
import java.lang.reflect.Type
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class SQLStorage(private val config: SkiesCratesConfig.Storage) : IStorage {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        StorageType.MYSQL -> MySQLProvider(config)
        StorageType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid storage type!")
    }
    private val cratesType: Type = object : TypeToken<HashMap<String, CrateData>>() {}.type
    private val keysType: Type = object : TypeToken<HashMap<String, Int>>() {}.type

    init {
        connectionProvider.init()
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = UserData(uuid)
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}userdata WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
                    userData.crates = SkiesCrates.INSTANCE.gson.fromJson(result.getString("crates"), cratesType)
                    userData.keys = SkiesCrates.INSTANCE.gson.fromJson(result.getString("keys"), keysType)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userData
    }

    override fun saveUser(userData: UserData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}userdata (uuid, crates, `keys`) VALUES ('%s', '%s', '%s')",
                    userData.uuid.toString(),
                    SkiesCrates.INSTANCE.gson.toJson(userData.crates),
                    SkiesCrates.INSTANCE.gson.toJson(userData.keys)
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUsedKey(uuid: UUID): UsedKeyData? {
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}used_keys WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
                    return UsedKeyData(
                        uuid,
                        result.getString("keyId"),
                        result.getLong("timeUsed"),
                        UUID.fromString(result.getString("player"))
                    )
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun saveUsedKey(usedKeyData: UsedKeyData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}used_keys (uuid, keyId, timeUsed, player) VALUES ('%s', '%s', %d, '%s')",
                    usedKeyData.uuid.toString(),
                    usedKeyData.keyId,
                    usedKeyData.timeUsed,
                    usedKeyData.player.toString()
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            try {
                getUser(uuid)
            } catch (e: Exception) {
                UserData(uuid)  // Return default data rather than throwing
            }
        }, SkiesCrates.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(userData)
        }, SkiesCrates.INSTANCE.asyncExecutor)
    }

    override fun getUsedKeyAsync(uuid: UUID): CompletableFuture<UsedKeyData?> {
        return CompletableFuture.supplyAsync({
            getUsedKey(uuid)
        }, SkiesCrates.INSTANCE.asyncExecutor)
    }

    override fun saveUsedKeyAsync(usedKeyData: UsedKeyData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUsedKey(usedKeyData)
        }, SkiesCrates.INSTANCE.asyncExecutor)
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
