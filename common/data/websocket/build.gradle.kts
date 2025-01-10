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
            implementation(projects.common)
            implementation(projects.common.data)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.common.must)
        }
    }
}