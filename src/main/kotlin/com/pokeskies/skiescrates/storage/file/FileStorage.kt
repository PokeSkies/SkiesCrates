package com.pokeskies.skiescrates.storage.file

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.IStorage
import java.util.*
import java.util.concurrent.CompletableFuture

class FileStorage : IStorage {
    private var fileData: FileData = ConfigManager.loadFile(STORAGE_FILENAME, FileData(), "", true)

    companion object {
        private const val STORAGE_FILENAME = "storage.json"
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = fileData.userdata[uuid]
        return userData ?: UserData(uuid)
    }

    override fun saveUser(userData: UserData): Boolean {
        fileData.userdata[userData.uuid] = userData
        val snapshot = HashMap(fileData.userdata)
        val fileDataCopy = FileData().apply { userdata = snapshot }
        return ConfigManager.saveFile(STORAGE_FILENAME, fileDataCopy)
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            getUser(uuid)
        }, SkiesCrates.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(userData)
        }, SkiesCrates.INSTANCE.asyncExecutor)
    }
}
