package org.example.project.nir

import java.awt.image.BufferedImage
import java.awt.Color
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.imageio.ImageIO

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

        // Calculamos la intensidad promedio de la columna SIN normalizar.
        // El resultado ahora estará en el rango [0.0, 255.0].
        val averageIntensity = columnIntensitySum / height

        // Mapeamos la columna 'x' a su longitud de onda correspondiente
        val wavelength = mapPixelToWavelength(pixelColumn = x, imageWidth = width).toDouble()

        spectrum.add(SpectrumPoint(wavelength = wavelength, intensity = averageIntensity))
    }

    return spectrum
}

fun mapPixelToWavelength(pixelColumn: Int, imageWidth: Int): Float {
    // Ejemplo de mapeo lineal: asume que el espectro va de 400nm a 1100nm
    val startWavelength = 300f
    val endWavelength = 900f
    return startWavelength + (pixelColumn.toFloat() / imageWidth) * (endWavelength - startWavelength)
}

fun saveSpectrumToCsv(data: List<SpectrumPoint>, path: String = ".") {
    if (data.isEmpty()) {
        println("Advertencia: No hay datos para guardar en el CSV.")
        return
    }

    val symbols = DecimalFormatSymbols(Locale.US)
    val wavelengthFormatter = DecimalFormat("0.00", symbols) // Formato para longitud de onda (2 decimales)
    val intensityFormatter = DecimalFormat("0.000000", symbols) // Formato para intensidad (6 decimales)

    // Genera un nombre de archivo único con fecha y hora
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val file = File(path, "espectro_$timestamp.csv")

    try {
        FileWriter(file).use { writer ->
            writer.append("Longitud de Onda;Intensidad\n")

            data.forEach { point ->
                val formattedWavelength = wavelengthFormatter.format(point.wavelength)
                val formattedIntensity = intensityFormatter.format(point.intensity)

                writer.append("$formattedWavelength;$formattedIntensity\n")
            }
        }
        println("Datos guardados exitosamente en: ${file.absolutePath}")

    } catch (e: Exception) {
        println("Error al guardar el archivo CSV: ${e.message}")
        e.printStackTrace()
    }
}

fun saveImageToFile(image: BufferedImage, path: String = ".") {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val file = File(path, "imagen_$timestamp.png")
    try {
        ImageIO.write(image, "PNG", file)
        println("Imagen guardada exitosamente en: ${file.absolutePath}")
    } catch (e: Exception) {
        println("Error al guardar la imagen: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Calcula el espectro promedio a partir de una lista de espectros.
 */
fun calculateAverageSpectrum(spectrums: List<List<SpectrumPoint>>): List<SpectrumPoint> {
    if (spectrums.isEmpty()) return emptyList()

    // Agrupa todos los puntos de todos los espectros por su longitud de onda.
    val groupedByWavelength = spectrums.flatten().groupBy { it.wavelength }

    // Para cada longitud de onda, calcula la intensidad promedio.
    return groupedByWavelength.map {
        val wavelength = it.key
        val points = it.value
        val averageIntensity = points.map { p -> p.intensity }.average()
        SpectrumPoint(wavelength, averageIntensity)
    }.sortedBy { it.wavelength } // Ordena el resultado final.
}

/**
 * Guarda el espectro promedio en un archivo CSV con un nombre único.
 */
fun saveAverageSpectrumToCsv(averageSpectrum: List<SpectrumPoint>, path: String = ".") {
    if (averageSpectrum.isEmpty()) {
        println("Advertencia: No hay datos de espectro promedio para guardar.")
        return
    }

    val symbols = DecimalFormatSymbols(Locale.US)
    val wavelengthFormatter = DecimalFormat("0.00", symbols)
    val intensityFormatter = DecimalFormat("0.000000", symbols)

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    // El nombre del archivo ahora sigue el formato "media_..."
    val file = File(path, "media_$timestamp.csv")

    try {
        FileWriter(file).use { writer ->
            writer.append("Longitud de Onda;Intensidad Promedio\n")

            averageSpectrum.forEach { point ->
                val formattedWavelength = wavelengthFormatter.format(point.wavelength)
                val formattedIntensity = intensityFormatter.format(point.intensity)
                writer.append("$formattedWavelength;$formattedIntensity\n")
            }
        }
        println("Espectro promedio guardado exitosamente en: ${file.absolutePath}")

    } catch (e: Exception) {
        println("Error al guardar el archivo CSV promedio: ${e.message}")
        e.printStackTrace()
    }
}
