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
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.kotlin.datetime)
            implementation(libs.sqlite.jdbc)
            implementation(projects.common)
            implementation(projects.client.model)
            implementation(projects.client.repository.local)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}