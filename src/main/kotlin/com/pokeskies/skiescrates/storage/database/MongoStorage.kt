package com.pokeskies.skiescrates.storage.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.connection.ClusterSettings
import com.pokeskies.skiescrates.config.ConfigManager
import com.pokeskies.skiescrates.config.LoggingOptions
import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.pokeskies.skiescrates.data.logging.RewardLog
import com.pokeskies.skiescrates.data.userdata.UserData
import com.pokeskies.skiescrates.storage.IStorage
import com.pokeskies.skiescrates.utils.Utils
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.IOException
import java.util.*

class MongoStorage(config: SkiesCratesConfig.Storage) : IStorage {
    private var mongoClient: MongoClient? = null
    private var mongoDatabase: MongoDatabase? = null
    private var userdataCollection: MongoCollection<UserData>? = null
    private var rewardLogsCollection: MongoCollection<RewardLog>? = null

    init {
        try {
            val credential = MongoCredential.createCredential(
                config.username,
                "admin",
                config.password.toCharArray()
            )
            var settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)

            settings = if (config.urlOverride.isNotEmpty()) {
                settings.applyConnectionString(ConnectionString(config.urlOverride))
            } else {
                settings
                    .credential(credential)
                    .applyToClusterSettings { builder: ClusterSettings.Builder ->
                        builder.hosts(listOf(ServerAddress(config.host, config.port)))
                    }
            }

            this.mongoClient = MongoClients.create(settings.build())

            val codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(UUIDCodec()),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            )

            this.mongoDatabase = mongoClient!!.getDatabase(config.database)
                .withCodecRegistry(codecRegistry)
            this.userdataCollection = this.mongoDatabase!!.getCollection("userdata", UserData::class.java)
            if (ConfigManager.CONFIG.logging.enabled && ConfigManager.CONFIG.logging.modes.contains(LoggingOptions.LogMode.STORAGE)) {
                this.rewardLogsCollection = this.mongoDatabase!!.getCollection("reward_logs", RewardLog::class.java)
            }
        } catch (e: Exception) {
            throw IOException("Error while attempting to setup Mongo Database: $e")
        }
    }

    override suspend fun getUser(uuid: UUID): UserData {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to fetch data from the Mongo database!")
            return UserData(uuid)
        }
        return userdataCollection?.find(Filters.eq("_id", uuid))?.firstOrNull() ?: UserData(uuid)
    }

    override suspend fun saveUser(uuid: UUID, userData: UserData): Boolean {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to save data to the Mongo database!")
            return false
        }
        val query = Filters.eq("_id", uuid.toString())
        val result = this.userdataCollection?.replaceOne(query, userData, ReplaceOptions().upsert(true))

        return result?.wasAcknowledged() ?: false
    }

    override fun writeCrateLog(log: RewardLog): Boolean {
        if (mongoDatabase == null || rewardLogsCollection == null) {
            Utils.printError("There was an error while attempting to write log to the Mongo database!")
            return false
        }
        return this.rewardLogsCollection?.insertOne(log)?.wasAcknowledged() ?: false
    }

    override fun close() {
        mongoClient?.close()
    }
}
