package org.example.project

import java.io.File

// --- ESTRUCTURAS DE DATOS ---
data class SpectrumPoint(val wavelength: Double, val intensity: Double)
data class Composition(val fat: Double, val protein: Double)

/**
 * Contiene todos los datos extraídos de un único archivo CSV de análisis.
 */
data class SpectrumData(val spectrumPoints: List<SpectrumPoint>, val liters: Double)


/**
 * Lee un archivo CSV de espectro y lo convierte en un objeto [SpectrumData].
 * Extrae tanto los puntos del espectro como los litros de leche.
 */
fun readSpectrumDataFromFile(file: File): SpectrumData {
    val lines = file.readLines()
    var liters = 0.0
    val spectrumPoints = mutableListOf<SpectrumPoint>()

    for (line in lines) {
        try {
            // Intenta extraer los litros
            if (line.trim().startsWith("Litros:", ignoreCase = true)) {
                liters = line.substringAfter(":").trim().replace(',', '.').toDouble()
                continue // Pasa a la siguiente línea
            }

            // Intenta extraer los puntos del espectro
            val parts = line.split(";")
            if (parts.size == 2) {
                val wavelength = parts[0].replace(',', '.').toDouble()
                val intensity = parts[1].replace(',', '.').toDouble()
                spectrumPoints.add(SpectrumPoint(wavelength, intensity))
            }
        } catch (e: NumberFormatException) {
            // Ignora las líneas que no se pueden parsear (como los encabezados)
            continue
        }
    }

    return SpectrumData(spectrumPoints, liters)
}
