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
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(projects.common)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}