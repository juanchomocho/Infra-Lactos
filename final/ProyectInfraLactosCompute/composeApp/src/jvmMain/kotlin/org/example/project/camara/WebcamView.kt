package org.example.project.camara

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import kotlinx.coroutines.delay


/**
 * Un selector de cámara que actualiza dinámicamente la lista de cámaras disponibles al hacer clic.
 */
@Composable
fun WebcamSelector(
    selectedWebcam: Webcam?,
    onWebcamSelected: (Webcam?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    // Estado para almacenar la lista de cámaras, que ahora se puede actualizar.
    var availableWebcams by remember { mutableStateOf<List<Webcam>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val updateWebcams: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            val freshWebcams = Webcam.getWebcams()
            withContext(Dispatchers.Main) {
                availableWebcams = freshWebcams
                // Comprobación: si la cámara seleccionada ya no existe, notificarlo.
                if (selectedWebcam != null && selectedWebcam.name !in freshWebcams.map { it.name }) {
                    // Selecciona la primera disponible, o null si no hay ninguna.
                    onWebcamSelected(freshWebcams.firstOrNull())
                }
            }
        }
    }

    // Carga la lista de cámaras la primera vez que se muestra el componente.
    LaunchedEffect(Unit) {
        updateWebcams()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                // Al hacer clic, actualiza la lista y luego muestra el menú.
                updateWebcams()
                expanded = true
            }
    ) {
        OutlinedTextField(
            value = selectedWebcam?.name ?: if (availableWebcams.isEmpty()) "No hay cámaras conectadas" else "Selecciona una cámara",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cámara") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Actualizar y abrir menú de cámaras"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false, // Mantiene la lógica del clic en el Box
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (availableWebcams.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Ninguna cámara disponible") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                availableWebcams.forEach { webcam ->
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
}


/**
 * Un componente que muestra el feed de video de una cámara específica.
 * (Sin cambios en este componente)
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

    LaunchedEffect(webcam) {
        imageBitmap = null
        launch(Dispatchers.IO) {
            while (webcam.isOpen) {
                val frame: BufferedImage? = webcam.image
                if (frame != null) {
                    imageBitmap = frame.toComposeImageBitmap()
                }
                delay(33)
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
        Box(modifier = modifier.background(Color.DarkGray)) {
            Text("Iniciando cámara: ${webcam.name}...", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}
