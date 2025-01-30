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
            implementation(libs.outcome)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.data.websocket)
            implementation(projects.server.model)
            implementation(projects.server.route)
            implementation(projects.client.remote)
            implementation(projects.client.websocket)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}