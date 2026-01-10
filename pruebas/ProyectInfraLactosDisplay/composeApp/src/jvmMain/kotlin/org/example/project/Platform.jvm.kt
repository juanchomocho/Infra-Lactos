package org.example.project

import java.io.File
import javax.swing.JFileChooser

actual fun isJvm(): Boolean = true

actual fun chooseDirectory(currentPath: String): String {
    val chooser = JFileChooser(currentPath).apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        dialogTitle = "Seleccionar Directorio de Guardado"
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
    } else {
        currentPath
    }
}

actual fun getDefaultDataPath(): String {
    return File(System.getProperty("user.home"), "ProyectInfraLactosReceivedData").absolutePath
}