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
            implementation(libs.kotlin.test)
        }
    }
}