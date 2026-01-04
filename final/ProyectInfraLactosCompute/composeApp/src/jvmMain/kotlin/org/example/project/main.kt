package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver


fun main() {
    Webcam.setDriver(WebcamDefaultDriver())
    application {
        Window(onCloseRequest = ::exitApplication, title = "Analizador de Espectro") {
            App()
        }
    }
}