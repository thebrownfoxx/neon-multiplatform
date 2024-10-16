plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(projects.common)
    implementation(libs.auth0.jwt)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.common.must)
    testImplementation(projects.common.hash)
    testImplementation(projects.server.repository)
    testImplementation(projects.server.repository.memory)
}