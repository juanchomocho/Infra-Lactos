package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val driver = createDriver()
    Window(
        onCloseRequest = { 
            driver.close()
            exitApplication()
        },
        title = "InfraLactos Server",
    ) {
        App(driver)
    }
}