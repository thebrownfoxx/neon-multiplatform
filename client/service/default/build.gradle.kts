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
            implementation(libs.kotlinx.datetime)
            implementation(libs.exposed.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.websockets)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.data.websocket)
            implementation(projects.common.data.websocket.ktor)
            implementation(projects.server.model)
            implementation(projects.server.route)
            implementation(projects.client.websocket)
            implementation(projects.client.model)
            implementation(projects.client.repository)
            implementation(projects.client.repository)
            implementation(projects.client.repository.exposed)
            implementation(projects.client.service)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}