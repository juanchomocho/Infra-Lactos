package org.example.project

import kotlin.random.Random

object IdentifierProvider {
    private val identifiers = mutableListOf<String>()
    private var currentIndex = 0

    fun initialize(yearOfBirth: Int) {
        if (identifiers.isNotEmpty()) return // Ya ha sido inicializado

        val yearDigits = yearOfBirth.toString().takeLast(2)
        val generatedCodes = mutableSetOf<String>()

        while (generatedCodes.size < 1000) {
            val randomFourDigits = Random.nextInt(1000, 10000).toString()
            val newId = "MDV$yearDigits$randomFourDigits"
            generatedCodes.add(newId)
        }
        
        identifiers.addAll(generatedCodes.shuffled())
        println("Generados ${identifiers.size} identificadores únicos.")
    }

    fun getNextIdentifier(): String? {
        if (currentIndex >= identifiers.size) {
            println("Error: No hay más identificadores disponibles.")
            return null // No hay más identificadores
        }
        return identifiers[currentIndex++]
    }
}
