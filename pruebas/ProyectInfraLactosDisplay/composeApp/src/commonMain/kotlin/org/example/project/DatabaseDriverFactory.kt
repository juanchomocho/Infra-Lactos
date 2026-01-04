package org.example.project

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.project.db.AppDatabase

object DatabaseDriverFactory {
    fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:infralactos_cache.db")
        try {
            // La primera vez que se conecta, crea el esquema si no existe.
            AppDatabase.Schema.create(driver)
        } catch (e: Exception) {
            println("Error al crear la base de datos: ${e.message}")
        }
        return driver
    }
}
