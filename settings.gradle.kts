pluginManagement {
    repositories {
        google()            // <-- potrzebne, tu jest Android Gradle Plugin
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GlyphWolf"
include(":app")
