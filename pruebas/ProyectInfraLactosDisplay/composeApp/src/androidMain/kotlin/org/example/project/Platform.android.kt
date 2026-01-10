package org.example.project

import java.io.File

// Implementación para Android.
actual fun isJvm(): Boolean = false

actual fun chooseDirectory(currentPath: String): String {
    // En Android, no mostramos un selector de directorios de escritorio.
    return currentPath
}

actual fun getDefaultDataPath(): String {
    val internalStoragePath = AndroidAppContext.context.filesDir.absolutePath
    return File(internalStoragePath, "ReceivedData").absolutePath
}