package org.example.project

// Puedes poner esta función en un archivo de utilidades
import java.awt.image.BufferedImage
import java.awt.Color
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Procesa una imagen y calcula la intensidad de cada columna de píxeles.
 * Devuelve la intensidad en valores absolutos (promedio de 0 a 255).
 *
 * @param image La imagen capturada por la cámara.
 * @return Una lista de SpectrumPoint con longitud de onda y su intensidad absoluta (0-255).
 */
fun generateDispersedSpectrum(image: BufferedImage): List<SpectrumPoint> {
    val width = image.width
    val height = image.height
    if (width == 0 || height == 0) return emptyList()

    val spectrum = mutableListOf<SpectrumPoint>()

    // Recorremos cada COLUMNA de píxeles
    for (x in 0 until width) {
        var columnIntensitySum = 0.0 // Usar Double para mayor precisión

        // Sumamos la intensidad de todos los píxeles de esa columna
        for (y in 0 until height) {
            val pixelColor = Color(image.getRGB(x, y))
            // Usamos el canal rojo como nuestra medida de intensidad (valor de 0 a 255)
            columnIntensitySum += pixelColor.red
        }

        // --- CAMBIO CLAVE ---
        // Calculamos la intensidad promedio de la columna SIN normalizar.
        // El resultado ahora estará en el rango [0.0, 255.0].
        val averageIntensity = columnIntensitySum / height

        // Mapeamos la columna 'x' a su longitud de onda correspondiente
        val wavelength = mapPixelToWavelength(pixelColumn = x, imageWidth = width).toDouble()

        spectrum.add(SpectrumPoint(wavelength = wavelength, intensity = averageIntensity))
    }

    return spectrum
}

// Asegúrate de que esta función también esté en tu archivo
// Esta es una función de ejemplo, la tuya podría ser diferente
fun mapPixelToWavelength(pixelColumn: Int, imageWidth: Int): Float {
    // Ejemplo de mapeo lineal: asume que el espectro va de 400nm a 1100nm
    val startWavelength = 400f
    val endWavelength = 1100f
    return startWavelength + (pixelColumn.toFloat() / imageWidth) * (endWavelength - startWavelength)
}

// Puedes colocar esta función en NIRUtilis.kt o en otro archivo de utilidades.

// Asegúrate de que SpectrumPoint esté definido y accesible
// data class SpectrumPoint(val wavelength: Double, val intensity: Double)

fun saveSpectrumToCsv(data: List<SpectrumPoint>, path: String = ".") {
    if (data.isEmpty()) {
        println("Advertencia: No hay datos para guardar en el CSV.")
        return
    }

    // Usamos Locale.US para asegurar que el punto sea el separador decimal.
    // Esto es crucial para que programas como Excel interpreten bien los números.
    val symbols = DecimalFormatSymbols(Locale.US)
    val wavelengthFormatter = DecimalFormat("0.00", symbols) // Formato para longitud de onda (2 decimales)
    val intensityFormatter = DecimalFormat("0.000000", symbols) // Formato para intensidad (6 decimales)

    // Genera un nombre de archivo único con fecha y hora
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val file = File(path, "espectro_$timestamp.csv")

    try {
        FileWriter(file).use { writer ->
            writer.append("Longitud de Onda;Intensidad\n") // <-- Usa ; en lugar de ,

            data.forEach { point ->
                val formattedWavelength = wavelengthFormatter.format(point.wavelength)
                val formattedIntensity = intensityFormatter.format(point.intensity)

                // CAMBIO 2: Cambia el separador de datos a punto y coma
                writer.append("$formattedWavelength;$formattedIntensity\n") // <-- Usa ; en lugar de ,
            }
        }
        println("Datos guardados exitosamente en: ${file.absolutePath}")

    } catch (e: Exception) {
        println("Error al guardar el archivo CSV: ${e.message}")
        e.printStackTrace()
    }
}