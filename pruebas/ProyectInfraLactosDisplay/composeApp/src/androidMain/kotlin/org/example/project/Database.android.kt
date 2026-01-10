package org.example.project

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.project.db.AppDatabase

actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(AppDatabase.Schema, AndroidAppContext.context, "infralactos_cache.db")
}