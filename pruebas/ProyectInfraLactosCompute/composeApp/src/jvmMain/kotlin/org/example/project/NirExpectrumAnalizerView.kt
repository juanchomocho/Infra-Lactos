// Archivo: NirExpectrumAnalizerView.kt
package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage

// Mueve esta clase de datos a un lugar accesible, quizás su propio archivo o uno común.
data class SpectrumPoint(val wavelength: Float, val intensity: Float)

// En NIRUtilis.kt o donde lo tengas definido

@Composable
fun DataAcquisitionEngine(
    webcam: Webcam?, // Recibe la cámara seleccionada (puede ser null)
    onDataUpdated: (List<SpectrumPoint>) -> Unit
) {
    // Si no hay cámara, no hacemos nada.
    if (webcam == null) return

    // Este efecto gestiona el ciclo de vida de la cámara.
    // Se ejecuta cuando 'webcam' cambia.
    DisposableEffect(webcam) {
        // Abre la cámara cuando el efecto se inicia.
        if (!webcam.isOpen) {
            println("Abriendo cámara para análisis: ${webcam.name}")
            webcam.open()
        }
        onDispose {
            // Cierra la cámara cuando el efecto se limpia (cambia la cámara o el composable desaparece).
            if (webcam.isOpen) {
                println("Cerrando cámara de análisis: ${webcam.name}")
                webcam.close()
            }
            // Limpiamos los datos al cerrar la cámara
            onDataUpdated(emptyList())
        }
    }

    // Este efecto captura datos mientras la cámara esté abierta.
    LaunchedEffect(webcam) {
        launch(Dispatchers.IO) {
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    onDataUpdated(generateDispersedSpectrum(frame))
                }
                delay(100) // Frecuencia de análisis
            }
        }
    }
}
