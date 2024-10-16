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
    }
}

include(":server")
include(":server:repository")
include(":server:repository:test")
include(":server:repository:memory")
include(":server:service")
include(":server:service:test")
include(":server:service:default")
include(":shared")
include(":common")
include(":common:must")
include(":common:hash")
include(":client:application")
include(":client:repository")
include(":client:repository:test")
include(":client:repository:memory")
include(":client:service")
include(":client:service:repository")