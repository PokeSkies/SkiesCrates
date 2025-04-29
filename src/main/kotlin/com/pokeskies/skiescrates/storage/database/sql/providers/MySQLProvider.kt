package com.pokeskies.skiescrates.storage.database.sql.providers

import com.pokeskies.skiescrates.config.SkiesCratesConfig
import com.zaxxer.hikari.HikariConfig

class MySQLProvider(val config: SkiesCratesConfig.Storage) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:mysql://%s:%d/%s",
        config.host,
        config.port,
        config.database
    )

    override fun getDriverClassName(): String = "com.mysql.cj.jdbc.Driver"
    override fun getDriverName(): String = "mysql"
    override fun configure(config: HikariConfig) {}
}
