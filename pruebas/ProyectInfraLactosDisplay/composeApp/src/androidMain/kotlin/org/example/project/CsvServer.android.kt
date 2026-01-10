package org.example.project

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class CsvServer(private val getSavePath: () -> String) {

    private var server: ApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    fun start() {
        if (isRunning) {
            println("El servidor ya está en funcionamiento.")
            return
        }
        isRunning = true

        // Iniciar el servidor sin bloquear el hilo
        server = embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
            routing {
                post("/upload-spectrum") {
                    try {
                        val csvContent = call.receiveText()
                        val savePath = getSavePath()
                        val identifier = csvContent.lines().firstOrNull { it.startsWith("ID Muestra:") }?.substringAfter(":")?.trim() ?: "unknown"
                        val fileName = "received_spectrum_${identifier}_${System.currentTimeMillis()}.csv"

                        val file = File(savePath, fileName)
                        file.parentFile.mkdirs()
                        file.writeText(csvContent)

                        val message = "CSV del identificador '$identifier' guardado en ${file.absolutePath}"
                        call.respondText(message)
                        println(message)
                    } catch (e: Exception) {
                        val errorMessage = "Error al guardar el archivo: ${e.message}"
                        call.respondText(errorMessage)
                        println(errorMessage)
                        e.printStackTrace()
                    }
                }
            }
        }.start(wait = false) // Corregido: wait = false para no bloquear

        println("Servidor iniciado en el puerto 8080")
        startDiscoveryBroadcast()
    }

    fun stop() {
        server?.stop(1000, 2000)
        serverScope.cancel()
        isRunning = false
        println("Servidor detenido.")
    }

    private fun startDiscoveryBroadcast() {
        serverScope.launch {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val message = "INFRALACTOS_SERVER".toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                val packet = DatagramPacket(message, message.size, broadcastAddress, 8888)

                while (isActive) {
                    try {
                        socket.send(packet)
                        kotlinx.coroutines.delay(5000)
                    } catch (e: Exception) {
                        // Silenciar errores de red en el broadcast
                    }
                }
            }
        }
    }
}