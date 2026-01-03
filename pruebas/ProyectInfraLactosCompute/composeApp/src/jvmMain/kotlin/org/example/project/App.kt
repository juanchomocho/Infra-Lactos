// Archivo: App.kt
package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.example.project.camara.WebcamSelector
import org.example.project.camara.WebcamView
import org.example.project.nir.* // Importar todo de nir
import java.awt.image.BufferedImage

@Composable
fun App() {
    var spectrumData by remember { mutableStateOf<List<SpectrumPoint>>(emptyList()) }
    var selectedWebcam by remember { mutableStateOf<Webcam?>(null) }
    var latestImage by remember { mutableStateOf<BufferedImage?>(null) }
    var isAcquisitionRunning by remember { mutableStateOf(false) }
    val sessionSpectrums = remember { mutableStateListOf<List<SpectrumPoint>>() }

    // Efecto que se ejecuta cuando el estado de adquisición cambia
    LaunchedEffect(isAcquisitionRunning) {
        if (isAcquisitionRunning) {
            // Bucle que se ejecuta mientras la adquisición esté activa
            while (isActive) {
                delay(5000) // Esperar 5 segundos
                if (spectrumData.isNotEmpty()) {
                    // Ya no guardamos el espectro individual, solo lo acumulamos en memoria
                    sessionSpectrums.add(spectrumData) // Añadir a la lista para el promedio
                    println("Espectro capturado para promedio. Total: ${sessionSpectrums.size}")
                }
            }
        }
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // Columna de control (izquierda)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp)
            ) {
                Text("Panel de Control", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(24.dp))

                WebcamSelector(
                    selectedWebcam = selectedWebcam,
                    onWebcamSelected = { newWebcam ->
                        if (selectedWebcam != newWebcam) {
                            isAcquisitionRunning = false // Detener si se cambia de cámara
                        }
                        selectedWebcam = newWebcam
                    }
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { saveSpectrumToCsv(spectrumData) },
                    enabled = spectrumData.isNotEmpty() && !isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)) // Azul oscuro
                ) {
                    Text("Guardar Espectro Actual")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { latestImage?.let { saveImageToFile(it) } },
                    enabled = latestImage != null && !isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)) // Azul oscuro
                ) {
                    Text("Guardar Imagen")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        sessionSpectrums.clear() // Limpiar datos de la sesión anterior
                        isAcquisitionRunning = true
                    },
                    enabled = selectedWebcam != null && !isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Verde oscuro
                ) {
                    Text("Start")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        isAcquisitionRunning = false
                        if (sessionSpectrums.isNotEmpty()) {
                            val averageSpectrum = calculateAverageSpectrum(sessionSpectrums)
                            saveAverageSpectrumToCsv(averageSpectrum)
                        }
                    },
                    enabled = isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)) // Rojo oscuro
                ) {
                    Text("Stop")
                }
            }

            // Columna de visualización (derecha)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                WebcamView(
                    webcam = selectedWebcam,
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) 
                Spacer(Modifier.height(16.dp))
                // Aquí podrías añadir un gráfico que muestre el espectro en tiempo real
            }
        }

        // El motor de adquisición SIEMPRE está activo si hay una cámara,
        // para que la vista previa y los datos en tiempo real funcionen.
        DataAcquisitionEngine(
            webcam = selectedWebcam,
            onDataUpdated = { newData ->
                spectrumData = newData
            },
            onImageUpdated = { newImage ->
                latestImage = newImage
            }
        )
    }
}
