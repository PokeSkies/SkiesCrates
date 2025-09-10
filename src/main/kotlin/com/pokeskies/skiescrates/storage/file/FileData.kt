package com.pokeskies.skiescrates.storage.file

import com.pokeskies.skiescrates.data.userdata.UserData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FileData {
    var userdata: MutableMap<UUID, UserData> = ConcurrentHashMap()
    override fun toString(): String {
        return "FileData(userdata=$userdata)"
    }
}
