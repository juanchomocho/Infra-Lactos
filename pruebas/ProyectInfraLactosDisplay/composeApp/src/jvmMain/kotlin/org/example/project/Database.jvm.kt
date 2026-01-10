package org.example.project

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.project.db.AppDatabase
import java.io.File

actual fun createDriver(): SqlDriver {
    val dbFile = File("infralactos_cache.db")
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (!dbFile.exists()) {
        AppDatabase.Schema.create(driver)
    }
    return driver
}