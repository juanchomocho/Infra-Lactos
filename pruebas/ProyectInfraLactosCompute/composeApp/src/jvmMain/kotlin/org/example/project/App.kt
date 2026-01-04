package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.camara.WebcamSelector
import org.example.project.camara.WebcamView
import org.example.project.nir.*
import java.awt.image.BufferedImage
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import kotlin.random.Random

private object AppPreferences {
    private const val PREF_OUTPUT_PATH = "outputPath"
    private val prefs: Preferences = Preferences.userNodeForPackage(AppPreferences::class.java)

    fun getOutputPath(default: String): String {
        return prefs.get(PREF_OUTPUT_PATH, default)
    }

    fun setOutputPath(path: String) {
        prefs.put(PREF_OUTPUT_PATH, path)
    }
}

sealed interface ConnectionState {
    data class Connecting(val attempt: Int) : ConnectionState
    data class Connected(val serverIp: String) : ConnectionState
    object Failed : ConnectionState
}

@Composable
fun App() {
    var sessionYear by remember { mutableStateOf<Int?>(null) }
    var spectrumData by remember { mutableStateOf<List<SpectrumPoint>>(emptyList()) }
    var selectedWebcam by remember { mutableStateOf<Webcam?>(null) }
    var latestImage by remember { mutableStateOf<BufferedImage?>(null) }
    var isAcquisitionRunning by remember { mutableStateOf(false) }
    val sessionSpectrums = remember { mutableStateListOf<List<SpectrumPoint>>() }
    val coroutineScope = rememberCoroutineScope()
    var currentIdentifier by remember { mutableStateOf<String?>(null) }
    var showIdentifier by remember { mutableStateOf(false) }

    // Load the saved path, or use a default if not found
    val defaultPath = File(System.getProperty("user.home"), "ProyectInfraLactosComputeData").absolutePath
    var outputPath by remember { mutableStateOf(AppPreferences.getOutputPath(defaultPath)) }
    val manualSavePath = File(outputPath, "Manual").absolutePath
    val sessionSavePath = File(outputPath, "Session").absolutePath
    val pendingUploadsSavePath = File(outputPath, "PendingUploads").absolutePath


    var connectionState by remember { mutableStateOf<ConnectionState>(ConnectionState.Connecting(1)) }

    // --- Efecto para descubrir el servidor en segundo plano ---
    LaunchedEffect(outputPath) {
        launch(Dispatchers.IO) {
            while (isActive) {
                val serverIp = discoverServer { attempt ->
                    withContext(Dispatchers.Main) {
                        connectionState = ConnectionState.Connecting(attempt)
                    }
                }

                if (serverIp != null) {
                    withContext(Dispatchers.Main) {
                        connectionState = ConnectionState.Connected(serverIp)
                    }
                    uploadPendingFiles(serverIp, pendingUploadsSavePath)
                    delay(120000) // Espera 2 minutos antes de volver a verificar
                } else {
                    withContext(Dispatchers.Main) {
                        connectionState = ConnectionState.Failed
                    }
                    println("Búsqueda fallida. Reintentando en 1 minuto...")
                    delay(60000)
                }
            }
        }
    }

    // --- Efecto para la captura de datos ---
    LaunchedEffect(isAcquisitionRunning) {
        if (isAcquisitionRunning) {
            println("Inicio de la captura en 25 segundos...")
            delay(25000)
            println("¡Captura iniciada!")
            while (isActive) {
                delay(5000)
                if (spectrumData.isNotEmpty()) {
                    sessionSpectrums.add(spectrumData)
                    println("Espectro capturado para promedio. Total: ${sessionSpectrums.size}")
                }
            }
        } else {
            // Limpia los datos de la sesión cuando se detiene la captura
            currentIdentifier = null
            sessionYear = null
        }
    }

    // --- Efecto para mostrar el ID con retraso ---
    LaunchedEffect(currentIdentifier) {
        if (currentIdentifier != null) {
            delay(3000) // Espera 3 segundos
            showIdentifier = true
        } else {
            showIdentifier = false
        }
    }

    // --- Efecto para crear directorios de salida ---
    LaunchedEffect(outputPath) {
        File(manualSavePath).mkdirs()
        File(sessionSavePath).mkdirs()
        File(pendingUploadsSavePath).mkdirs()
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(300.dp)
            ) {
                Text("Panel de Control", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                ConnectionStatus(connectionState)
                Spacer(Modifier.height(16.dp))

                // Muestra el identificador actual en dos columnas (con retraso)
                currentIdentifier?.let {
                    if (showIdentifier) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ID Muestra:", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                WebcamSelector(
                    selectedWebcam = selectedWebcam,
                    onWebcamSelected = { newWebcam ->
                        if (selectedWebcam != newWebcam) {
                            isAcquisitionRunning = false
                        }
                        selectedWebcam = newWebcam
                    }
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val chooser = JFileChooser()
                        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                        chooser.dialogTitle = "Seleccionar Directorio de Salida"
                        val result = chooser.showOpenDialog(null) // null parent
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val newPath = chooser.selectedFile.absolutePath
                            outputPath = newPath
                            // Save the selected path to preferences
                            AppPreferences.setOutputPath(newPath)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Elegir Directorio")
                }
                Spacer(Modifier.height(8.dp))
                Text("Directorio de salida:", style = MaterialTheme.typography.bodySmall)
                Text(File(outputPath).absolutePath, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = { saveSpectrumToCsv(spectrumData, manualSavePath) },
                    enabled = spectrumData.isNotEmpty() && !isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Text("Guardar Espectro Actual")
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { latestImage?.let { saveImageToFile(it, manualSavePath) } },
                    enabled = latestImage != null && !isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Text("Guardar Imagen")
                }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val randomYear = Random.nextInt(1990, 2016)
                        sessionYear = randomYear
                        IdentifierProvider.initialize(randomYear)
                        currentIdentifier = IdentifierProvider.getNextIdentifier()

                        sessionSpectrums.clear()
                        isAcquisitionRunning = true
                    },
                    enabled = selectedWebcam != null && !isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Start")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        isAcquisitionRunning = false
                        if (sessionSpectrums.isNotEmpty() && currentIdentifier != null) {
                            val id = currentIdentifier!!
                            val averageSpectrum = calculateAverageSpectrum(sessionSpectrums)
                            saveAverageSpectrumToCsv(averageSpectrum, id, pendingUploadsSavePath) // For upload
                            saveAverageSpectrumToCsv(averageSpectrum, id, sessionSavePath) // For user

                            val currentConnectionState = connectionState
                            if (currentConnectionState is ConnectionState.Connected) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    uploadPendingFiles(currentConnectionState.serverIp, pendingUploadsSavePath)
                                }
                            }
                        }
                    },
                    enabled = isAcquisitionRunning,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Stop y Guardar")
                }
            }

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

@Composable
fun ConnectionStatus(state: ConnectionState) {
    val (color, text) = when (state) {
        is ConnectionState.Connecting -> Color.Yellow to "Conectando... (intento ${state.attempt})"
        is ConnectionState.Connected -> Color.Green to "Conectado"
        is ConnectionState.Failed -> Color.Red to "Fallo de conexión"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

suspend fun discoverServer(onAttempt: suspend (Int) -> Unit): String? = withContext(Dispatchers.IO) {
    DatagramSocket(8888).use { socket ->
        socket.soTimeout = 5000
        val buffer = ByteArray(1024)
        val packet = DatagramPacket(buffer, buffer.size)

        for (attempt in 1..6) {
            onAttempt(attempt)
            try {
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)
                if (message == "INFRALACTOS_SERVER") {
                    println("Servidor encontrado en: ${packet.address.hostAddress}")
                    return@withContext packet.address.hostAddress
                }
            } catch (e: SocketTimeoutException) {
                println("Intento $attempt fallido, reintentando...")
            }
        }

        println("No se encontró el servidor después de 30 segundos.")
        return@withContext null
    }
}

suspend fun uploadPendingFiles(serverIp: String, pendingFilesPath: String) {
    println("Buscando archivos pendientes para subir en: $pendingFilesPath")
    val pendingDir = File(pendingFilesPath)
    if (!pendingDir.exists()) return

    val filesToSend = pendingDir.listFiles { _, name -> name.endsWith(".csv") } ?: return

    for (file in filesToSend) {
        try {
            val content = file.readText() // Handles closing the stream
            val serverUrl = "http://$serverIp:8080/upload-spectrum"
            val success = sendCsvOverNetwork(serverUrl, content)

            if (success) {
                println("Archivo ${file.name} enviado con éxito. Borrando archivo local.")

                // Make deletion more robust
                var deleted = false
                for (attempt in 1..3) {
                    if (file.delete()) {
                        deleted = true
                        break
                    }
                    // Wait and suggest garbage collection if deletion fails.
                    delay(200L * attempt)
                    System.gc()
                }

                if (deleted) {
                    println("Borrado exitoso: ${file.name}")
                } else {
                    println("Error: no se pudo borrar el archivo ${file.name}")
                }
            } else {
                println("Fallo al enviar ${file.name}. Se reintentará más tarde.")
            }
        } catch (e: Exception) {
            println("Error procesando el archivo ${file.name}: ${e.message}")
            e.printStackTrace()
        }
    }
}
