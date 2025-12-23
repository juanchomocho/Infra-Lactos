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

@Composable
fun App() {
    // Este estado contendrá los datos más recientes del espectro.
    var spectrumData by remember { mutableStateOf<List<SpectrumPoint>>(emptyList()) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Visor de Espectro y Captura")
            Spacer(modifier = Modifier.height(16.dp))

            // Botón para guardar los datos actuales
            Button(onClick = {
                // Llama a la función de guardado con los datos que tenemos en el estado.
                saveSpectrumToCsv(spectrumData)
            }) {
                Text("Guardar Espectro en CSV")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aquí podrías tener un gráfico que muestre 'spectrumData' en tiempo real.
            // Por ejemplo: SpectrumChart(data = spectrumData)

            // El motor de adquisición se ejecuta en segundo plano y actualiza el estado.
            DataAcquisitionEngine(
                onDataUpdated = { newData ->
                    spectrumData = newData
                }
            )
        }
    }
}
