// composeApp/src/jvmMain/kotlin/org/example/project/WebcamView.kt

package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage

/**
 * Un componente de UI que muestra una lista desplegable de cámaras y notifica cuando una es seleccionada.
 */
@Composable
fun WebcamSelector(
    webcams: List<Webcam>,
    selectedWebcam: Webcam?,
    onWebcamSelected: (Webcam) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    if (webcams.isEmpty()) {
        Text("No se encontraron cámaras.", color = Color.Red)
        return
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedWebcam?.name ?: "Selecciona una cámara",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cámara") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            webcams.forEach { webcam ->
                DropdownMenuItem(
                    text = { Text(webcam.name) },
                    onClick = {
                        onWebcamSelected(webcam)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Un componente que muestra el feed de video de una cámara específica.
 * La cámara debe estar abierta para que muestre algo.
 */
@Composable
fun WebcamView(webcam: Webcam?, modifier: Modifier = Modifier) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    if (webcam == null) {
        Box(modifier = modifier.background(Color.Black)) {
            Text("Ninguna cámara seleccionada", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    // Este LaunchedEffect se reiniciará si 'webcam' cambia
    LaunchedEffect(webcam) {
        // Limpiamos la imagen anterior al cambiar de cámara
        imageBitmap = null
        launch(Dispatchers.IO) {
            // El stream de imágenes solo funcionará si la cámara está abierta.
            // DataAcquisitionEngine se encargará de abrirla y cerrarla.
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    imageBitmap = frame.toComposeImageBitmap()
                }
                delay(33) // ~30 FPS para la vista previa, menos intensivo que 16ms
            }
        }
    }

    val currentImage = imageBitmap
    if (currentImage != null) {
        Image(
            bitmap = currentImage,
            contentDescription = "Vista de la Webcam",
            modifier = modifier
        )
    } else {
        // Placeholder mientras la cámara se inicia o si no hay imagen
        Box(modifier = modifier.background(Color.DarkGray)) {
            Text("Iniciando cámara: ${webcam.name}...", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}
