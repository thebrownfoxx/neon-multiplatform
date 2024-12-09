plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.outcome)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.client.converter)
            implementation(projects.client.model)
            implementation(projects.client.repository)
            implementation(projects.client.repository.local)
            implementation(projects.client.repository.remote)
            implementation(projects.server.model)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}