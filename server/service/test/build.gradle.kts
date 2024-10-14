import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.datetime)
            implementation(projects.common)
            implementation(projects.common.must)
            implementation(projects.common.hash)
            implementation(projects.server.service)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlin.test.wasmjs)
        }
        jvmMain.dependencies {
            implementation(libs.kotlin.test.junit)
        }
    }
}