package com.pokeskies.skiescrates.storage.file

import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.IStorage
import java.util.*

class FileStorage : IStorage {
    private var fileData: FileData = ConfigManager.loadFile(STORAGE_FILENAME, FileData(), "", true)

    companion object {
        private const val STORAGE_FILENAME = "storage.json"
    }

    override suspend fun getUser(uuid: UUID): UserData {
        val userData = fileData.userdata[uuid]
        return userData ?: UserData(uuid)
    }

    override suspend fun saveUser(uuid: UUID, userData: UserData): Boolean {
        fileData.userdata[uuid] = userData
        return ConfigManager.saveFile(STORAGE_FILENAME, fileData)
    }
}
