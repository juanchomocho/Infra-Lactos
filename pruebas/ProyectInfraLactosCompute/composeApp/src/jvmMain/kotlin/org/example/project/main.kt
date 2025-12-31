package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver


fun main() {
    // --- LÍNEA CLAVE ---
    // Fuerza a la librería a usar el driver "built-in" que es más estable
    // y no depende de librerías nativas externas como BridJ.
    // DEBE ejecutarse ANTES de cualquier llamada a Webcam.getWebcams()
    Webcam.setDriver(WebcamDefaultDriver()) // <-- AÑADE ESTA LÍNEA

    // El resto de tu aplicación
    application {
        Window(onCloseRequest = ::exitApplication, title = "Analizador de Espectro") {
            App()
        }
    }
}