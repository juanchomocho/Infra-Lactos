package org.example.project

// Puedes poner esta función en un archivo de utilidades
import java.awt.image.BufferedImage
import java.awt.Color

// Esta es una nueva estructura de datos para hacer el código más limpio.
data class SpectrumPoint(val wavelength: Float, val intensity: Float)

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
        val averageIntensity = (columnIntensitySum / height) / 255f

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

