import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17" // ensure ProGuard compatibility
        }
    }

    jvmToolchain(17) // compilation JDK
    sourceSets {
        all {
            languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            implementation("com.github.sarxos:webcam-capture:0.3.12")
            implementation("org.slf4j:slf4j-nop:1.7.32")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(compose.desktop.common)

            implementation("io.ktor:ktor-client-core:2.3.12")
            implementation("io.ktor:ktor-client-cio:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            implementation("io.ktor:ktor-client-logging:2.3.12")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            // Build Windows .msi, macOS .dmg, Linux .deb
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "InfraLactosApp"
            packageVersion = "1.0.0"
            description = "InfraLactos Desktop Application"
            vendor = "MyCompany"

            // Windows-specific installer options
            windows {
                menuGroup = "InfraLactos"
                shortcut = true
                upgradeUuid = "123e4567-e89b-12d3-a456-426614174000" // unique UUID for installer upgrades
                console = false
            }
        }
    }
}
