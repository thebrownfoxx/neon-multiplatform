plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.get().toInt())
    }
}

dependencies {
    implementation(libs.outcome)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.auth0.jwt)
    implementation(projects.common)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.common.must)
    testImplementation(projects.common.hash)
    implementation(projects.server.model)
    testImplementation(projects.server.repository)
    testImplementation(projects.server.repository.inmemory)
}