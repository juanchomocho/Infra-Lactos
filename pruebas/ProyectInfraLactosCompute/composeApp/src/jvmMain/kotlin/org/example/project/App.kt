// Archivo: App.kt (o tu archivo principal de UI)
package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.github.sarxos.webcam.Webcam

@Composable
fun App() {
    var spectrumData by remember { mutableStateOf<List<SpectrumPoint>>(emptyList()) }

    // 1. Obtenemos la lista de cámaras y la guardamos en el estado principal.
    val webcams = remember { Webcam.getWebcams() }

    // 2. Este es el estado "subido". App ahora sabe qué cámara está seleccionada.
    var selectedWebcam by remember { mutableStateOf<Webcam?>(webcams.firstOrNull()) }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // Columna de control (izquierda)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp) // Ancho fijo para el panel de control
            ) {
                Text("Panel de Control", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(24.dp))

                // 3. Usamos el nuevo WebcamSelector. Le pasamos el estado y la función para notificar cambios.
                WebcamSelector(
                    webcams = webcams,
                    selectedWebcam = selectedWebcam,
                    onWebcamSelected = { newWebcam ->
                        selectedWebcam = newWebcam
                    }
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { saveSpectrumToCsv(spectrumData) },
                    // Deshabilitado si no hay datos o no hay cámara
                    enabled = selectedWebcam != null && spectrumData.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Espectro en CSV")
                }
            }

            // Columna de visualización (derecha)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f) // Ocupa el resto del espacio
            ) {
                // 4. WebcamView ahora solo muestra el feed de la cámara seleccionada.
                WebcamView(
                    webcam = selectedWebcam,
                    modifier = Modifier.fillMaxWidth().height(300.dp) // Tamaño para el visor
                )

                Spacer(Modifier.height(16.dp))

                // Aquí puedes añadir tu gráfico del espectro.
                // SpectrumChart(data = spectrumData, modifier = Modifier.weight(1f))
            }
        }

        // 5. El motor de adquisición se ejecuta en segundo plano. No es visible, solo procesa datos.
        // Recibe la cámara seleccionada y actualiza los datos.
        DataAcquisitionEngine(
            webcam = selectedWebcam,
            onDataUpdated = { newData ->
                spectrumData = newData
            }
        )
    }
}
