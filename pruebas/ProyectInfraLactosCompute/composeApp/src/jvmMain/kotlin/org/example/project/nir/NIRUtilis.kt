package org.example.project.nir

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
 */
fun generateDispersedSpectrum(image: BufferedImage): List<SpectrumPoint> {
    val width = image.width
    val height = image.height
    if (width == 0 || height == 0) return emptyList()

    val spectrum = mutableListOf<SpectrumPoint>()

    for (x in 0 until width) {
        var columnIntensitySum = 0.0
        for (y in 0 until height) {
            val pixelColor = Color(image.getRGB(x, y))
            columnIntensitySum += pixelColor.red
        }
        val averageIntensity = columnIntensitySum / height
        val wavelength = mapPixelToWavelength(pixelColumn = x, imageWidth = width).toDouble()
        spectrum.add(SpectrumPoint(wavelength = wavelength, intensity = averageIntensity))
    }

    return spectrum
}

fun mapPixelToWavelength(pixelColumn: Int, imageWidth: Int): Float {
    val startWavelength = 300f
    val endWavelength = 900f
    return startWavelength + (pixelColumn.toFloat() / imageWidth) * (endWavelength - startWavelength)
}

/**
 * Genera el contenido de un archivo CSV a partir de una lista de SpectrumPoint.
 */
fun generateCsvContent(data: List<SpectrumPoint>, header: String): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val wavelengthFormatter = DecimalFormat("0.00", symbols)
    val intensityFormatter = DecimalFormat("0.000000", symbols)

    return buildString {
        append(header)
        data.forEach { point ->
            val formattedWavelength = wavelengthFormatter.format(point.wavelength)
            val formattedIntensity = intensityFormatter.format(point.intensity)
            append("$formattedWavelength;$formattedIntensity\n")
        }
    }
}

fun saveSpectrumToCsv(data: List<SpectrumPoint>, path: String = ".") {
    if (data.isEmpty()) {
        println("Advertencia: No hay datos para guardar en el CSV.")
        return
    }

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val file = File(path, "espectro_$timestamp.csv")
    val content = generateCsvContent(data, "Longitud de Onda;Intensidad\n")

    try {
        FileWriter(file).use { it.write(content) }
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

fun calculateAverageSpectrum(spectrums: List<List<SpectrumPoint>>): List<SpectrumPoint> {
    if (spectrums.isEmpty()) return emptyList()

    val groupedByWavelength = spectrums.flatten().groupBy { it.wavelength }

    return groupedByWavelength.map {
        val wavelength = it.key
        val points = it.value
        val averageIntensity = points.map { p -> p.intensity }.average()
        SpectrumPoint(wavelength, averageIntensity)
    }.sortedBy { it.wavelength }
}

fun saveAverageSpectrumToCsv(averageSpectrum: List<SpectrumPoint>, path: String = ".") {
    if (averageSpectrum.isEmpty()) {
        println("Advertencia: No hay datos de espectro promedio para guardar.")
        return
    }

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val file = File(path, "media_$timestamp.csv")
    val content = generateCsvContent(averageSpectrum, "Longitud de Onda;Intensidad Promedio\n")

    try {
        FileWriter(file).use { it.write(content) }
        println("Espectro promedio guardado exitosamente en: ${file.absolutePath}")
    } catch (e: Exception) {
        println("Error al guardar el archivo CSV promedio: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Envía el contenido de un CSV a través de la red usando una petición POST.
 */
suspend fun sendCsvOverNetwork(url: String, csvContent: String) {
    val client = HttpClient(CIO)
    try {
        println("Enviando datos a $url...")
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Text.CSV)
            setBody(csvContent)
        }
        println("Respuesta del servidor: ${response.status}")
    } catch (e: Exception) {
        println("Error al enviar los datos por red: ${e.message}")
    } finally {
        client.close()
    }
}
