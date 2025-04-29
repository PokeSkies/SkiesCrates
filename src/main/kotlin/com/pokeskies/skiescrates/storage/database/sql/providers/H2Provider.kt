package com.pokeskies.skiescrates.storage.database.sql.providers

import com.pokeskies.skiescrates.SkiesCrates
import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.zaxxer.hikari.HikariConfig
import java.io.File

class H2Provider(config: SkiesCratesConfig.Storage) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:h2:%s;AUTO_SERVER=TRUE",
        File(SkiesCrates.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.h2.Driver"
    override fun getDriverName(): String = "h2"
    override fun configure(config: HikariConfig) {}
}
