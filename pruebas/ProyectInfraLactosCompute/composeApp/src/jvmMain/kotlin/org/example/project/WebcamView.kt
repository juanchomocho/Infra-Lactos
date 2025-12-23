package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage

@Composable
fun WebcamView(modifier: Modifier = Modifier) {
    // 1. Estados para manejar la selección de cámara
    val webcams: List<Webcam> = remember { Webcam.getWebcams() }
    var selectedWebcam by remember { mutableStateOf<Webcam?>(webcams.firstOrNull()) }
    var isCameraOpen by remember { mutableStateOf(false) }

    // Estado para el menú desplegable
    var expanded by remember { mutableStateOf(false) }

    if (webcams.isEmpty()) {
        Text("No se encontró ninguna cámara.", modifier = modifier.fillMaxSize())
        return
    }

    // --- Interfaz para seleccionar la cámara ---
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Button(onClick = { expanded = true }) {
                Text(selectedWebcam?.name ?: "Selecciona una cámara")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                webcams.forEach { webcam ->
                    DropdownMenuItem(onClick = {
                        selectedWebcam = webcam
                        expanded = false
                        isCameraOpen = false // Cierra la anterior si estaba abierta
                    }) {
                        Text(webcam.name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para iniciar/detener la vista de la cámara
        Button(onClick = { isCameraOpen = !isCameraOpen }, enabled = selectedWebcam != null) {
            Text(if (isCameraOpen) "Detener Cámara" else "Iniciar Cámara")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Vista de la cámara (solo si está activa) ---
        if (isCameraOpen && selectedWebcam != null) {
            CameraFeed(webcam = selectedWebcam!!, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CameraFeed(webcam: Webcam, modifier: Modifier = Modifier) {
    // 2. DisposableEffect para abrir y cerrar la cámara seleccionada de forma segura.
    DisposableEffect(webcam) {
        if (!webcam.isOpen) {
            webcam.open()
        }
        println("Cámara abierta: ${webcam.name}")

        onDispose {
            if (webcam.isOpen) {
                webcam.close()
            }
            println("Cámara cerrada: ${webcam.name}")
        }
    }

    // 3. Estado para almacenar el último frame de la cámara.
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // 4. LaunchedEffect para capturar imágenes continuamente.
    LaunchedEffect(webcam) {
        launch(Dispatchers.IO) {
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    imageBitmap = frame.toComposeImageBitmap()
                }
                delay(16) // Aprox. 60 FPS
            }
        }
    }

    // 5. Mostramos la imagen en la UI.
    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Vista de la Webcam",
            modifier = modifier.fillMaxWidth()
        )
    }
}
