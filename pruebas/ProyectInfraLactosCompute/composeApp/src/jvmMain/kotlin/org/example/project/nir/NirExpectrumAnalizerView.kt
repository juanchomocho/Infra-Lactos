package org.example.project.nir

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage

/**
 * Un motor de adquisición de datos que se ejecuta en segundo plano.
 * Recibe una cámara, la abre, captura imágenes, las procesa y notifica los nuevos datos.
 * Cierra la cámara automáticamente cuando ya no se necesita.
 */
@Composable
fun DataAcquisitionEngine(
    webcam: Webcam?, // Recibe la cámara seleccionada (puede ser null)
    onDataUpdated: (List<SpectrumPoint>) -> Unit,
    onImageUpdated: (BufferedImage) -> Unit // Nuevo callback para la imagen
) {
    // Si no hay cámara, no hacemos nada.
    if (webcam == null) {
        onDataUpdated(emptyList())
        return
    }

    // Este efecto gestiona el ciclo de vida de la cámara. Se ejecuta cuando \'webcam\' cambia.
    DisposableEffect(webcam) {
        // Abre la cámara cuando el efecto se inicia.
        if (!webcam.isOpen) {
            println("Abriendo cámara para análisis: ${webcam.name}")
            webcam.open()
        }
        onDispose {
            // Cierra la cámara cuando el efecto se limpia (el usuario cambia de cámara o el composable desaparece).
            if (webcam.isOpen) {
                println("Cerrando cámara de análisis: ${webcam.name}")
                webcam.close()
            }
        }
    }

    // Este efecto captura datos mientras la cámara esté abierta.
    LaunchedEffect(webcam) {
        launch(Dispatchers.IO) {
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    // Notificar la nueva imagen
                    onImageUpdated(frame)
                    val spectrum = generateDispersedSpectrum(frame)
                    onDataUpdated(spectrum)
                }
                delay(100) // Frecuencia de análisis del espectro
            }
        }
    }
}