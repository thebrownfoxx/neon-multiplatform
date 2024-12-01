plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization)
            implementation(libs.exposed.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.websockets)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.websocket)
            implementation(projects.common.websocket.ktor)
            implementation(projects.server.model)
            implementation(projects.server.route)
            implementation(projects.client.websocket)
            implementation(projects.client.model)
            implementation(projects.client.repository)
            implementation(projects.client.repository.local)
            implementation(projects.client.repository.local.exposed)
            implementation(projects.client.repository.remote)
            implementation(projects.client.repository.remote.websocket)
            implementation(projects.client.repository.offlinefirst)
            implementation(projects.client.service)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}