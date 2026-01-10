package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

actual class ServerManager {
    private var server: CsvServer? = null

    actual fun start(getSavePath: () -> String) {
        server = CsvServer(getSavePath)
        server?.start()
    }

    actual fun stop() {
        server?.stop()
    }
}

@Composable
actual fun rememberServerManager(): ServerManager {
    val serverManager = remember { ServerManager() }
    DisposableEffect(Unit) {
        onDispose { serverManager.stop() }
    }
    return serverManager
}