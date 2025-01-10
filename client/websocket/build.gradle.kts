plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.get().toInt())
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.outcome)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client.websockets)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.data.websocket)
            implementation(projects.common.data.websocket.ktor)
            implementation(projects.server.route)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}