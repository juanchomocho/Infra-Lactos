package org.example.project

// Los imports de UI como 'Text' y 'Modifier' ya no son estrictamente necesarios aquí
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage

@Composable
fun DataAcquisitionEngine() { // Renombrado para reflejar su nuevo propósito
    val webcam: Webcam? = remember { Webcam.getWebcams().firstOrNull() }
    // El estado sigue siendo útil para mantener los datos, aunque no se dibuje
    var spectrumData by remember { mutableStateOf<List<SpectrumPoint>>(emptyList()) }

    // Si no se encuentra ninguna cámara, imprime en consola y detiene la ejecución.
    if (webcam == null) {
        println("Error: No se encontró ninguna cámara.")
        return
    }

    // El ciclo de vida de la cámara sigue siendo CRUCIAL.
    DisposableEffect(webcam) {
        webcam.open()
        println("Cámara abierta: ${webcam.name}. Iniciando captura de datos...")
        onDispose {
            webcam.close()
            println("Cámara cerrada. Deteniendo captura de datos.")
        }
    }

    // El LaunchedEffect ahora es el corazón de la aplicación.
    LaunchedEffect(webcam) {
        launch(Dispatchers.IO) {
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    // 1. Procesa la imagen para obtener los datos.
                    val newSpectrumData = generateDispersedSpectrum(frame)
                    spectrumData = newSpectrumData

                    // 2. AQUÍ ES DONDE OBTIENES LOS DATOS.
                    // Por ahora, los imprimimos en la consola.
                    println("Datos del espectro capturados: $spectrumData")

                    // Aquí podrías hacer otras cosas con los datos:
                    // - saveDataToFile(spectrumData)
                    // - sendDataToNetwork(spectrumData)
                }
                // Esperamos un tiempo para la siguiente captura.
                // Auméntalo si no necesitas datos tan a menudo (ej. 1000L para 1 vez por segundo).
                delay(100)
            }
        }
    }
}

