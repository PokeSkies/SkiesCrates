package com.pokeskies.skiescrates.storage

import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.database.MongoStorage
import com.pokeskies.skiescrates.storage.database.sql.SQLStorage
import com.pokeskies.skiescrates.storage.file.FileStorage
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

    fun getUser(uuid: UUID): UserData
    fun saveUser(uuid: UUID, userData: UserData): Boolean

    fun getUserAsync(uuid: UUID): CompletableFuture<UserData>
    fun saveUserAsync(uuid: UUID, userData: UserData): CompletableFuture<Boolean>

    fun close() {}
}
