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
            implementation(libs.exposed.core)
            implementation(libs.exposed.kotlin.datetime)
            implementation(projects.common)
            implementation(projects.common.hash)
            implementation(projects.common.data)
            implementation(projects.common.data.exposed)
            implementation(projects.server.model)
            implementation(projects.server.repository)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}