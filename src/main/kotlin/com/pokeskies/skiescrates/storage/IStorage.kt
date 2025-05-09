package com.pokeskies.skiescrates.storage

import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.database.MongoStorage
import com.pokeskies.skiescrates.storage.database.sql.SQLStorage
import com.pokeskies.skiescrates.storage.file.FileStorage
import java.util.*

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

    suspend fun getUser(uuid: UUID): UserData
    suspend fun saveUser(uuid: UUID, userData: UserData): Boolean

    fun close() {}
}
