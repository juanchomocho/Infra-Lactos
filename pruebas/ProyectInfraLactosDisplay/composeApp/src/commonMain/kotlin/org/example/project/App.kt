package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.db.AppDatabase
import java.io.File
import java.text.DecimalFormat
import javax.swing.JFileChooser
import java.util.prefs.Preferences

private object AppPreferences {
    private const val PREF_OUTPUT_PATH = "outputPath"
    private val prefs: Preferences = Preferences.userRoot().node("org.example.project.infralactos.server")

    fun getOutputPath(default: String): String = prefs.get(PREF_OUTPUT_PATH, default)
    fun setOutputPath(path: String) = prefs.put(PREF_OUTPUT_PATH, path)
}

@Composable
fun App() {
    val defaultPath = File(System.getProperty("user.home"), "ProyectInfraLactosReceivedData").absolutePath
    var outputPath by remember { mutableStateOf(AppPreferences.getOutputPath(defaultPath)) }

    val server = remember { CsvServer { outputPath } }
    val database = remember { AppDatabase(DatabaseDriverFactory.createDriver()) }
    val repository = remember { AnalysisRepository(database) }

    DisposableEffect(Unit) {
        server.start()
        onDispose { server.stop() }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Servidor", "Ovejas")

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title -> Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title) }) }
            }
            when (selectedTabIndex) {
                0 -> ServerStatusScreen(outputPath) { newPath ->
                    outputPath = newPath
                    AppPreferences.setOutputPath(newPath)
                }
                1 -> SheepManagementScreen(outputPath, repository)
            }
        }
    }
}

@Composable
fun ServerStatusScreen(outputPath: String, onOutputPathChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Servidor InfraLactos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("El servidor se está ejecutando.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        Text("Directorio de guardado:", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(outputPath, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onOutputPathChange(chooseDirectory(outputPath)) }) { Text("Elegir Directorio") }
    }
}

fun chooseDirectory(currentPath: String): String {
    val chooser = JFileChooser(currentPath).apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        dialogTitle = "Seleccionar Directorio de Guardado"
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else currentPath
}

@Composable
fun SheepManagementScreen(outputPath: String, repository: AnalysisRepository) {
    var selectedSheepId by remember { mutableStateOf<String?>(null) }

    if (selectedSheepId == null) {
        SheepListScreen(outputPath) { selectedSheepId = it }
    } else {
        SheepDetailScreen(sheepId = selectedSheepId!!, repository = repository, outputPath = outputPath) { selectedSheepId = null }
    }
}

@Composable
fun SheepListScreen(outputPath: String, onSheepSelected: (String) -> Unit) {
    var sheepIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(outputPath) {
        isLoading = true
        sheepIds = withContext(Dispatchers.IO) {
            try {
                File(outputPath).takeIf { it.exists() && it.isDirectory }?.listFiles { _, name -> name.endsWith(".csv") }
                    ?.mapNotNull { file -> file.useLines { lines -> lines.firstOrNull { it.startsWith("ID Muestra:") }?.substringAfter(":")?.trim() } }
                    ?.toSet()?.sorted() ?: emptyList()
            } catch (e: Exception) {
                println("Error al leer los archivos de ovejas: ${e.message}")
                emptyList()
            }
        }
        isLoading = false
    }

    val filteredSheepIds = remember(searchQuery, sheepIds) { if (searchQuery.isBlank()) sheepIds else sheepIds.filter { it.contains(searchQuery, ignoreCase = true) } }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            sheepIds.isEmpty() -> Text("No se encontraron datos de ovejas.", modifier = Modifier.align(Alignment.Center))
            else -> Column {
                OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Buscar por MDV") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), singleLine = true)
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp)) { Text("Nº de Identificación (MDV)", style = MaterialTheme.typography.titleSmall) }
                if (filteredSheepIds.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("No se encontraron ovejas con ese identificador.") }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredSheepIds) { id ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onSheepSelected(id) }.padding(horizontal = 8.dp, vertical = 12.dp)) { Text(id) }
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SheepDetailScreen(sheepId: String, repository: AnalysisRepository, outputPath: String, onBack: () -> Unit) {
    var summary by remember { mutableStateOf<SheepAnalysisResult?>(null) }
    var history by remember { mutableStateOf<List<AnalysisResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(sheepId, outputPath) {
        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            val summaryJob = launch { summary = repository.getAnalysis(sheepId, outputPath) }
            val historyJob = launch { history = repository.getAnalysisHistory(sheepId, outputPath) }
            summaryJob.join()
            historyJob.join()
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) { Text("Atrás") }
            Spacer(Modifier.width(16.dp))
            Text("Análisis de: $sheepId", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            summary?.let { SummaryCard(it) }
            Spacer(Modifier.height(24.dp))
            HistoryTable(history)
        }
    }
}

@Composable
private fun SummaryCard(summary: SheepAnalysisResult) {
    val formatter = remember { DecimalFormat("0.00") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resultados del Promedio Ponderado", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
            InfoRow("Grasa Prom. Ponderada (%):", formatter.format(summary.weightedAverageFat))
            InfoRow("Proteína Prom. Ponderada (%):", formatter.format(summary.weightedAverageProtein))
            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))
            InfoRow("Total Leche Producida (L):", formatter.format(summary.totalLiters))
            InfoRow("Número de Muestras:", summary.fileCount.toString())
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
        Text(value, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HistoryTable(history: List<AnalysisResult>) {
    val formatter = remember { DecimalFormat("0.00") }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Historial de Análisis", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        if (history.isEmpty()) {
            Text("No se encontró historial para esta oveja.")
        } else {
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(8.dp)) {
                Text("Archivo", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Grasa (%)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Proteína (%)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Litros (L)", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            LazyColumn {
                items(history) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)) {
                        Text(it.file.name, modifier = Modifier.weight(2f), maxLines = 1)
                        Text(formatter.format(it.composition.fat), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text(formatter.format(it.composition.protein), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text(formatter.format(it.liters), modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                    }
                    Divider()
                }
            }
        }
    }
}