plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
    implementation(libs.auth0.jwt)
    implementation(projects.common)
    implementation(projects.common.hash)
    implementation(projects.server.service)
    implementation(projects.server.repository)
    testImplementation(libs.kotlin.test)
}