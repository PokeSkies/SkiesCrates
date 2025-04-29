package com.pokeskies.skiescrates.storage.file

import com.pokeskies.skiescrates.data.userdata.UserData
import java.util.*

class FileData {
    var userdata: HashMap<UUID, UserData> = HashMap()
    override fun toString(): String {
        return "FileData(userdata=$userdata)"
    }
}
