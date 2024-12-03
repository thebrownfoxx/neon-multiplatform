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
            implementation(libs.exposed.kotlin.datetime)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.data.exposed)
            implementation(projects.client.model)
            implementation(projects.client.repository.local)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}