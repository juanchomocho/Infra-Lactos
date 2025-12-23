// Archivo: NirExpectrumAnalizerView.kt
package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Mueve esta clase de datos a un lugar accesible, quizás su propio archivo o uno común.
data class SpectrumPoint(val wavelength: Float, val intensity: Float)

@Composable
fun DataAcquisitionEngine(
    // El estado ahora se pasa desde el padre, así sabemos qué guardar.
    onDataUpdated: (List<SpectrumPoint>) -> Unit
) {
    val webcam: Webcam? = remember { Webcam.getWebcams().firstOrNull() }

    if (webcam == null) {
        println("Error: No se encontró ninguna cámara.")
        return
    }

    DisposableEffect(webcam) {
        if (!webcam.isOpen) {
            webcam.open()
            println("Cámara abierta: ${webcam.name}. Iniciando captura de datos...")
        }
        onDispose {
            if (webcam.isOpen) {
                webcam.close()
                println("Cámara cerrada. Deteniendo captura de datos.")
            }
        }
    }

    LaunchedEffect(webcam) {
        launch(Dispatchers.IO) {
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    val newSpectrumData = generateDispersedSpectrum(frame) // Asumo que esta función existe
                    // En lugar de actualizar un estado local, notificamos al padre de los nuevos datos.
                    onDataUpdated(newSpectrumData)
                }
                delay(100) // Un delay más corto para datos más fluidos, ajústalo si es necesario.
            }
        }
    }
}