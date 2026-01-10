package org.example.project

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("infralactos_prefs", Context.MODE_PRIVATE)

    actual fun getOutputPath(default: String): String {
        return prefs.getString(PREF_OUTPUT_PATH, default) ?: default
    }

    actual fun setOutputPath(path: String) {
        prefs.edit().putString(PREF_OUTPUT_PATH, path).apply()
    }

    companion object {
        private const val PREF_OUTPUT_PATH = "outputPath"
    }
}

@Composable
actual fun rememberPreferencesManager(): PreferencesManager {
    val context = LocalContext.current
    return remember { PreferencesManager(context) }
}
