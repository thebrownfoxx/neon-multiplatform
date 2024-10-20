@file:Suppress("UnstableApiUsage")

rootProject.name = "Neon"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

include(":common")
include(":common:must")
include(":common:hash")
include(":server:application")
include(":server:model")
include(":server:repository")
include(":server:repository:test")
include(":server:repository:inmemory")
include(":server:service")
include(":server:service:test")
include(":server:service:default")
include(":client:application")
include(":client:service")
include(":client:service:default")