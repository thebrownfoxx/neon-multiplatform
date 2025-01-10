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
            implementation(libs.auth0.jwt)
            implementation(projects.common)
            implementation(projects.common.data)
            implementation(projects.common.hash)
            implementation(projects.server.model)
            implementation(projects.server.service)
            implementation(projects.server.repository)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}