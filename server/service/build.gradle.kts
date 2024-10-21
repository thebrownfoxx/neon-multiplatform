plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.auth0.jwt)
    implementation(projects.common)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.common.must)
    testImplementation(projects.common.hash)
    testImplementation(projects.server.repository)
    testImplementation(projects.server.repository.inmemory)
}