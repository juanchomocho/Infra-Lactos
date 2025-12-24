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
 * Procesa una imagen de una cámara NIR con óptica de dispersión.
 * Calcula la intensidad promedio para cada columna de píxeles.
 *
 * @param image La imagen capturada por la cámara.
 * @return Una lista de puntos SpectrumPoint, donde cada punto tiene una longitud de onda (nm) y su intensidad.
 */
fun generateDispersedSpectrum(image: BufferedImage): List<SpectrumPoint> {
    val width = image.width
    val height = image.height
    if (width == 0 || height == 0) return emptyList()

    val spectrum = mutableListOf<SpectrumPoint>()

    // Recorremos cada COLUMNA de píxeles
    for (x in 0 until width) {
        var columnIntensitySum = 0f
        // Sumamos la intensidad de todos los píxeles de esa columna
        for (y in 0 until height) {
            val pixelColor = Color(image.getRGB(x, y))
            // Usamos el canal rojo como nuestra medida de intensidad
            columnIntensitySum += pixelColor.red
        }

        // Calculamos la intensidad promedio de la columna y la normalizamos (0-255 -> 0-1)
        val averageIntensity = (columnIntensitySum * 100 / height) / 255

        // Mapeamos la columna 'x' a su longitud de onda correspondiente
        val wavelength = mapPixelToWavelength(pixelColumn = x, imageWidth = width)

        spectrum.add(SpectrumPoint(wavelength = wavelength, intensity = averageIntensity))
    }

    return spectrum
}


// Pon esta función en un archivo de utilidades o junto a las otras.

/**
 * Mapea una columna de píxeles (eje X de la imagen) a una longitud de onda en nanómetros (nm).
 *
 * @param pixelColumn La columna del píxel (de 0 al ancho de la imagen - 1).
 * @param imageWidth El ancho total de la imagen.
 * @param minWavelength La longitud de onda correspondiente a la primera columna (ej. 700 nm).
 * @param maxWavelength La longitud de onda correspondiente a la última columna (ej. 1100 nm).
 * @return La longitud de onda calculada en nanómetros.
 */
fun mapPixelToWavelength(
    pixelColumn: Int,
    imageWidth: Int,
    minWavelength: Float = 700f,
    maxWavelength: Float = 1100f
): Float {
    if (imageWidth <= 1) return minWavelength
    val range = maxWavelength - minWavelength
    return minWavelength + (pixelColumn.toFloat() / (imageWidth - 1)) * range
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