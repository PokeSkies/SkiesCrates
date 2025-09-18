package com.pokeskies.skiescrates.storage.database.sql

import com.google.gson.reflect.TypeToken
import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.pokeskies.skiescrates.data.userdata.CrateData
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

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            try {
                val result = getUser(uuid)
                result
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

    override fun close() {
        connectionProvider.shutdown()
    }
}
