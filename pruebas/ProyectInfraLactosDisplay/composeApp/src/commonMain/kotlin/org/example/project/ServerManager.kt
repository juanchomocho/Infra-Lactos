package org.example.project

import androidx.compose.runtime.Composable

expect class ServerManager {
    fun start(getSavePath: () -> String)
    fun stop()
}

@Composable
expect fun rememberServerManager(): ServerManager
