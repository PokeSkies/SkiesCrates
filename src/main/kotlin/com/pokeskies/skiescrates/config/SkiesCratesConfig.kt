package com.pokeskies.skiescrates.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiescrates.storage.StorageType

class SkiesCratesConfig(
    var debug: Boolean = false,
    @SerializedName("interaction_limiter")
    var interactionLimiter: Long = 1000,
    var storage: Storage = Storage(),
    var keys: KeyOptions = KeyOptions(),
) {
    class Storage(
        val type: StorageType = StorageType.JSON,
        val host: String = "",
        val port: Int = 3306,
        val database: String = "skiescrates",
        val username: String = "root",
        val password: String = "",
        val properties: Map<String, String> = mapOf("useUnicode" to "true", "characterEncoding" to "utf8"),
        @SerializedName("pool_settings")
        val poolSettings: StoragePoolSettings = StoragePoolSettings(),
        @SerializedName("url_override")
        val urlOverride: String = ""
    ) {
        override fun toString(): String {
            return "Storage(type=$type, host='$host', port=$port, database='$database', username='$username', " +
                    "password='$password', properties=$properties, poolSettings=$poolSettings, urlOverride='$urlOverride')"
        }
    }

    class StoragePoolSettings(
        @SerializedName("maximum_pool_size")
        val maximumPoolSize: Int = 10,
        @SerializedName("minimum_idle")
        val minimumIdle: Int = 10,
        @SerializedName("keepalive_time")
        val keepaliveTime: Long = 0,
        @SerializedName("connection_timeout")
        val connectionTimeout: Long = 30000,
        @SerializedName("idle_timeout")
        val idleTimeout: Long = 600000,
        @SerializedName("max_lifetime")
        val maxLifetime: Long = 1800000
    ) {
        override fun toString(): String {
            return "StoragePoolSettings(maximumPoolSize=$maximumPoolSize, minimumIdle=$minimumIdle," +
                    " keepaliveTime=$keepaliveTime, connectionTimeout=$connectionTimeout," +
                    " idleTimeout=$idleTimeout, maxLifetime=$maxLifetime)"
        }
    }

    override fun toString(): String {
        return "SkiesCratesConfig(debug=$debug, interactionLimiter=$interactionLimiter, storage=$storage, keys=$keys)"
    }
}
