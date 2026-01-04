package org.example.project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.project.db.AppDatabase
import java.io.File

// --- ESTRUCTURAS DE DATOS PARA LOS RESULTADOS ---

// Resultado PROMEDIO (cacheable)
data class SheepAnalysisResult(
    val mdv: String,
    val weightedAverageFat: Double,
    val weightedAverageProtein: Double,
    val totalLiters: Double,
    val fileCount: Int
)

// Resultado INDIVIDUAL (no cacheable)
data class AnalysisResult(val file: File, val composition: Composition, val liters: Double)


class AnalysisRepository(database: AppDatabase) {

    private val queries = database.appDatabaseQueries

    /**
     * Obtiene el análisis promedio para una oveja, usando la caché si es posible.
     * Ahora calcula la media ponderada por litros.
     */
    suspend fun getAnalysis(mdv: String, outputPath: String): SheepAnalysisResult? = withContext(Dispatchers.IO) {
        val outputDir = File(outputPath)
        if (!outputDir.exists()) return@withContext null

        val relevantFiles = outputDir.listFiles { _, name -> name.endsWith(".csv") }
            ?.filter { it.readLines().any { line -> line.contains("ID Muestra: $mdv") } }
            ?: return@withContext null

        val currentFileCount = relevantFiles.size
        if (currentFileCount == 0) return@withContext null

        val cachedAnalysis = queries.selectByMdv(mdv).executeAsOneOrNull()

        if (cachedAnalysis != null && cachedAnalysis.fileCount == currentFileCount.toLong()) {
            println("Resultado para $mdv obtenido de la caché.")
            return@withContext SheepAnalysisResult(
                mdv = cachedAnalysis.mdv,
                weightedAverageFat = cachedAnalysis.weightedAverageFat,
                weightedAverageProtein = cachedAnalysis.weightedAverageProtein,
                totalLiters = cachedAnalysis.totalLiters,
                fileCount = cachedAnalysis.fileCount.toInt()
            )
        }

        println("Calculando nuevo promedio ponderado para $mdv...")
        val allAnalyses = relevantFiles.map {
            val spectrumData = readSpectrumDataFromFile(it)
            val composition = PredictionModel.predictComposition(spectrumData.spectrumPoints)
            AnalysisResult(it, composition, spectrumData.liters)
        }

        val totalLiters = allAnalyses.sumOf { it.liters }
        if (totalLiters == 0.0) return@withContext null // Evitar división por cero

        val weightedFatSum = allAnalyses.sumOf { it.composition.fat * it.liters }
        val weightedProteinSum = allAnalyses.sumOf { it.composition.protein * it.liters }

        val weightedAverageFat = weightedFatSum / totalLiters
        val weightedAverageProtein = weightedProteinSum / totalLiters

        queries.insertOrUpdate(mdv, weightedAverageFat, weightedAverageProtein, totalLiters, currentFileCount.toLong())

        return@withContext SheepAnalysisResult(mdv, weightedAverageFat, weightedAverageProtein, totalLiters, currentFileCount)
    }

    /**
     * Obtiene el historial de todos los análisis individuales para una oveja.
     */
    suspend fun getAnalysisHistory(mdv: String, outputPath: String): List<AnalysisResult> = withContext(Dispatchers.IO) {
        File(outputPath).listFiles { _, name -> name.endsWith(".csv") }
            ?.filter { file -> file.readLines().any { it.contains("ID Muestra: $mdv") } }
            ?.map { file ->
                val spectrumData = readSpectrumDataFromFile(file)
                val composition = PredictionModel.predictComposition(spectrumData.spectrumPoints)
                AnalysisResult(file, composition, spectrumData.liters)
            }
            ?.sortedByDescending { it.file.lastModified() } ?: emptyList()
    }
}
