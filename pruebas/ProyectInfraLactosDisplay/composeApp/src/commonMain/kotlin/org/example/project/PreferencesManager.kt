package org.example.project

import androidx.compose.runtime.Composable

/**
 * Un gestor de preferencias multiplataforma para guardar y leer la ruta de salida.
 */
expect class PreferencesManager {
    fun getOutputPath(default: String): String
    fun setOutputPath(path: String)
}

@Composable
expect fun rememberPreferencesManager(): PreferencesManager
