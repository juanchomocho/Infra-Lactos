package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Esta es la data class que definimos anteriormente para mantener los datos organizados
// data class SpectrumPoint(val wavelength: Float, val intensity: Float)

@Composable
fun SpectrumChart(data: List<SpectrumPoint>) {

    // Usamos el Composable Canvas, que nos da un "lienzo" para dibujar.
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Si no hay al menos 2 puntos, no podemos dibujar una línea.
        if (data.size < 2) {
            return@Canvas
        }

        val canvasWidth = size.width
        val canvasHeight = size.height

        // --- Dibuja los ejes X e Y (opcional, pero ayuda a visualizar) ---
        drawLine(
            start = Offset(x = 0f, y = canvasHeight),
            end = Offset(x = canvasWidth, y = canvasHeight),
            color = Color.Gray,
            strokeWidth = 2f
        )
        drawLine(
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = 0f, y = canvasHeight),
            color = Color.Gray,
            strokeWidth = 2f
        )

        // --- Prepara la ruta (el Path) que dibujará la línea del gráfico ---
        val path = Path()

        // Obtenemos los rangos de los datos para poder mapearlos al tamaño del canvas.
        val minWavelength = data.first().wavelength
        val maxWavelength = data.last().wavelength
        val wavelengthRange = maxWavelength - minWavelength

        // Función para mapear un valor de un rango a otro, como en tu ejemplo.
        fun Float.mapToRange(inMin: Float, inMax: Float, outMin: Float, outMax: Float): Float {
            return (this - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
        }

        // 1. Mueve el lápiz al primer punto del gráfico.
        val firstX = data.first().wavelength.mapToRange(minWavelength, maxWavelength, 0f, canvasWidth)
        val firstY = data.first().intensity.mapToRange(0f, 1f, canvasHeight, 0f) // La intensidad (0-1) se mapea a la altura del canvas.
        path.moveTo(firstX, firstY)

        // 2. Itera sobre el resto de los puntos para dibujar las líneas.
        for (i in 1 until data.size) {
            val point = data[i]
            val x = point.wavelength.mapToRange(minWavelength, maxWavelength, 0f, canvasWidth)
            val y = point.intensity.mapToRange(0f, 1f, canvasHeight, 0f) // Y=0 está arriba, Y=canvasHeight está abajo.
            path.lineTo(x, y)
        }

        // 3. Dibuja la línea completa en el canvas.
        drawPath(
            path = path,
            color = Color.Red,
            style = Stroke(width = 3f) // Stroke define que se dibuje solo el contorno (una línea).
        )
    }
}