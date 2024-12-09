plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.outcome)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(projects.common)
    implementation(projects.common.data)
    implementation(projects.common.hash)
    implementation(projects.server.model)
    testImplementation(libs.kotlin.test)
}