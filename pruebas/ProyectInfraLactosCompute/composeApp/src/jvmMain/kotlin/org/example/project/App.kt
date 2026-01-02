// Archivo: App.kt
package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.sarxos.webcam.Webcam
import java.awt.image.BufferedImage

@Composable
fun App() {
    var spectrumData by remember { mutableStateOf<List<SpectrumPoint>>(emptyList()) }
    var selectedWebcam by remember { mutableStateOf<Webcam?>(null) }
    var latestImage by remember { mutableStateOf<BufferedImage?>(null) }

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
                        // --- LÓGICA CLAVE MODIFICADA ---
                        // 1. Siempre cerramos la cámara actual al interactuar con el selector.
                        // Esto dispara el onDispose en DataAcquisitionEngine.
                        selectedWebcam = null

                        // 2. Asignamos la nueva cámara.
                        // Si el usuario eligió una cámara, se abrirá.
                        // Si se desconectó y newWebcam es null, permanecerá cerrada.
                        selectedWebcam = newWebcam
                    }
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { saveSpectrumToCsv(spectrumData) },
                    enabled = selectedWebcam != null && spectrumData.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Espectro en CSV")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { latestImage?.let { saveImageToFile(it) } },
                    enabled = latestImage != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Imagen")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
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

                // Aquí el gráfico del espectro.
            }
        }

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
