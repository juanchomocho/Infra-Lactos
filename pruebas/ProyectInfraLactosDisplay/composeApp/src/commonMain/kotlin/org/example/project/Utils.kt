package org.example.project

import java.io.File
import java.util.regex.Pattern

// --- ESTRUCTURAS DE DATOS ---
data class SpectrumPoint(val wavelength: Double, val intensity: Double)
data class Composition(val fat: Double, val protein: Double)

/**
 * Contiene todos los datos extraídos de un único archivo CSV de análisis.
 */
data class SpectrumData(val spectrumPoints: List<SpectrumPoint>, val liters: Double)

/**
 * Lee un archivo CSV de espectro y lo convierte en un objeto [SpectrumData].
 * Esta versión es mucho más robusta y utiliza expresiones regulares.
 */
fun readSpectrumDataFromFile(file: File): SpectrumData {
    val lines = file.readLines()

    // Regex para encontrar un número (entero o decimal con , o .) en una línea
    val numberPattern = Pattern.compile("""(\d+([,.]\d+)?)""")

    // 1. Encontrar y extraer los litros de forma segura usando regex
    val liters = lines.firstNotNullOfOrNull { line ->
        if (line.contains("Litros", ignoreCase = true)) {
            val matcher = numberPattern.matcher(line)
            if (matcher.find()) {
                try {
                    matcher.group(1).replace(',', '.').toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    } ?: 0.0 // Si no se encuentra, se asume 0.0

    // 2. Encontrar y procesar las líneas del espectro
    val spectrumPoints = lines.mapNotNull { line ->
        try {
            val parts = line.split(";")
            if (parts.size == 2) {
                val wavelength = parts[0].replace(',', '.').toDouble()
                val intensity = parts[1].replace(',', '.').toDouble()
                SpectrumPoint(wavelength, intensity)
            } else {
                null // No es una línea de espectro válida
            }
        } catch (e: NumberFormatException) {
            null // Ignorar líneas que no son numéricas (cabeceras, etc.)
        }
    }

    return SpectrumData(spectrumPoints, liters)
}
