package org.example.project

import java.util.Locale
import kotlin.math.abs

/**
 * SIMULACIÓN DE UN MODELO PREDICTIVO
 *
 * ATENCIÓN: Este no es un modelo científico real. Es una simulación que genera
 * valores de grasa y proteína basados en los datos del espectro para imitar el
 * comportamiento de un modelo de calibración real.
 *
 * Reemplaza la lógica de este archivo por tu modelo de calibración cuando lo tengas.
 */
object PredictionModel {

    // Constantes arbitrarias para la simulación
    private const val FAT_FACTOR_1 = 0.015
    private const val FAT_FACTOR_2 = 4.5
    private const val PROTEIN_FACTOR_1 = 0.008
    private const val PROTEIN_FACTOR_2 = 3.0

    /**
     * Simula la predicción de porcentajes de grasa y proteína a partir de un espectro.
     * @param spectrum La lista de puntos (longitud de onda, intensidad) del espectro.
     * @return Un objeto [Composition] con los valores simulados de grasa y proteína.
     */
    fun predictComposition(spectrum: List<SpectrumPoint>): Composition {
        if (spectrum.isEmpty()) {
            return Composition(0.0, 0.0)
        }

        // Lógica de simulación simple y determinista.
        // Usamos la suma de las intensidades y el hashcode para generar variabilidad.
        val totalIntensity = spectrum.sumOf { it.intensity }
        val spectrumHash = abs(spectrum.hashCode() % 1000) / 1000.0 // Valor entre 0.0 y 1.0

        // Simulación para la grasa
        val fat = (totalIntensity * FAT_FACTOR_1 + spectrumHash) % 5 + FAT_FACTOR_2

        // Simulación para la proteína
        val protein = (totalIntensity * PROTEIN_FACTOR_1 + spectrumHash) % 3 + PROTEIN_FACTOR_2

        // Se redondea a dos decimales para que parezca un resultado real
        // Usamos Locale.US para asegurar que el separador decimal sea un punto (.)
        return Composition(
            fat = String.format(Locale.US, "%.2f", fat).toDouble(),
            protein = String.format(Locale.US, "%.2f", protein).toDouble()
        )
    }
}