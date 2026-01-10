package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

actual class PreferencesManager {
    private val prefs: Preferences = Preferences.userRoot().node("org.example.project.infralactos.server")

    actual fun getOutputPath(default: String): String {
        return prefs.get(PREF_OUTPUT_PATH, default)
    }

    actual fun setOutputPath(path: String) {
        prefs.put(PREF_OUTPUT_PATH, path)
    }

    companion object {
        private const val PREF_OUTPUT_PATH = "outputPath"
    }
}

@Composable
actual fun rememberPreferencesManager(): PreferencesManager {
    return remember { PreferencesManager() }
}
