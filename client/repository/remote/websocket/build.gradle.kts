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
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.websocket)
            implementation(projects.common.websocket.ktor)
            implementation(projects.client.websocket)
            implementation(projects.client.repository.remote)
            implementation(projects.server.model)
            implementation(projects.server.route)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}