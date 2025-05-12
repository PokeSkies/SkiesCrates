package com.pokeskies.skiescrates.storage

import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.pokeskies.skiescrates.data.logging.CrateLogEntry
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.database.MongoStorage
import com.pokeskies.skiescrates.storage.database.sql.SQLStorage
import com.pokeskies.skiescrates.storage.file.FileStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.*
import java.util.concurrent.CompletableFuture

interface IStorage {
    companion object {
        fun load(config: SkiesCratesConfig.Storage): IStorage {
            return when (config.type) {
                StorageType.JSON -> FileStorage()
                StorageType.MONGO -> MongoStorage(config)
                StorageType.MYSQL, StorageType.SQLITE -> SQLStorage(config)
            }
        }
    }

    // Userdata
    suspend fun getUser(uuid: UUID): UserData
    suspend fun saveUser(uuid: UUID, userData: UserData): Boolean

    // Logging
    suspend fun writeCrateLog(log: CrateLogEntry): Boolean
    fun writeCrateLogAsync(log: CrateLogEntry): CompletableFuture<Boolean> = GlobalScope.future {
        writeCrateLog(log)
    }

    fun close() {}
}
