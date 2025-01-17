import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
}

tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.get().toInt())
    }

    androidTarget()
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.outcome)
            implementation(libs.compose.windowSizeClass)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.exposed.core)
            implementation(libs.exposed.jdbc)
            implementation(libs.sqlite.jdbc)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.data.websocket)
            implementation(projects.server.model)
            implementation(projects.server.route)
            implementation(projects.client.model)
            implementation(projects.client.websocket)
            implementation(projects.client.repository)
            implementation(projects.client.repository.exposed)
            implementation(projects.client.service)
            implementation(projects.client.service.default)
            implementation(projects.client.service.remote)
            implementation(projects.client.service.offlinefirst)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.material3)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "com.thebrownfoxx.neon.client.application"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.thebrownfoxx.neon.client.application"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

compose.desktop {
    application {
        mainClass = "com.thebrownfoxx.neon.client.application.MainKt"

        buildTypes.release.proguard {
            version.set("7.6.1")
            configurationFiles.from(projectDir.resolve("compose-desktop.pro"))
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            includeAllModules = true
            packageName = "com.thebrownfoxx.neon.client.application"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

buildkonfig {
    packageName = "com.thebrownfoxx.neon.client.application"

    defaultConfigs {
        buildConfigFields(properties("local.properties"))
    }

    targetConfigs {
        create("desktop") {
            buildConfigFields(properties("desktop.local.properties"))
        }
        create("android") {
            buildConfigFields(properties("android.local.properties"))
        }
    }
}

fun properties(path: String): Properties {
    val propertiesFile = projectDir.resolve(path)
    val properties = Properties()
    if (propertiesFile.exists()) {
        properties.load(FileInputStream(propertiesFile))
    }
    return properties
}

fun TargetConfigDsl.buildConfigFields(properties: Properties) {
    buildConfigField(STRING, "HOST", properties.getProperty("HOST") ?: "")
    buildConfigField(STRING, "PORT", properties.getProperty("PORT") ?: "")
    buildConfigField(STRING, "LOCAL_PATH", properties.getProperty("LOCAL_PATH") ?: "")
}