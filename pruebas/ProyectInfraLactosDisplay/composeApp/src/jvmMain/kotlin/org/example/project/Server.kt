package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

    private var server: NettyApplicationEngine? = null
    private val discoveryScope = CoroutineScope(Dispatchers.IO)

    fun start() {
        if (server?.application?.isActive == true) {
            println("El servidor ya está en funcionamiento.")
            return
        }

        server = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            routing {
                post("/upload-spectrum") {
                    try {
                        val csvContent = call.receiveText()
                        val savePath = getSavePath()
                        // Extraer el ID de la muestra del contenido del CSV
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
        }.start(wait = false)

        println("Servidor iniciado en el puerto 8080")
        startDiscoveryBroadcast()
    }

    fun stop() {
        discoveryScope.cancel()
        server?.stop(1000, 2000)
        println("Servidor detenido.")
    }

    private fun startDiscoveryBroadcast() {
        discoveryScope.launch {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val message = "INFRALACTOS_SERVER".toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                val packet = DatagramPacket(message, message.size, broadcastAddress, 8888)

                while (isActive) {
                    try {
                        socket.send(packet)
                        // println("Enviando broadcast de descubrimiento...") // Comentado para no saturar la consola
                        kotlinx.coroutines.delay(5000) // Enviar cada 5 segundos
                    } catch (e: Exception) {
                        // println("Error al enviar broadcast: ${e.message}")
                    }
                }
            }
        }
    }
}