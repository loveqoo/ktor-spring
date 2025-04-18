rootProject.name = "ktor-spring"

// @Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        //maven("https://plugins.gradle.org/m2/")
        //maven("https://kotlin.bintray.com/kotlinx")
    }
}
