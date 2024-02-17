import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val plugins = libs.plugins
    application
    alias(plugins.kotlin.jvm)
    alias(plugins.kotlin.ksp)
    alias(plugins.ktor.plugin)
    alias(plugins.kotlin.spring)
    alias(plugins.kotlin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.deps)
    implementation(libs.spring.context)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logback.classic)
    implementation(libs.akkurate.core)
    implementation(libs.akkurate.ksp.plugin)
    ksp(libs.akkurate.ksp.plugin)
    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.kotlin.test.junit)
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }
}
