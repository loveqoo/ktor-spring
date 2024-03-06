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
    implementation(libs.arrow.core)
    implementation(libs.akkurate.core)
    implementation(libs.akkurate.ksp.plugin)
    implementation(libs.r2dbc.h2)
    implementation(libs.r2dbc.pool)
    implementation(libs.kotlinx.datetime)
    platform(libs.komapper.platform).let {
        implementation(it)
        ksp(it)
    }
    implementation(libs.komapper.starter.r2dbc)
    implementation(libs.komapper.dialect.h2.r2dbc)
    ksp(libs.komapper.processor)
    // implementation(libs.kotysa.r2dbc)
    // implementation(libs.bundles.exposed)
    // implementation(libs.mysql.connector)
    // implementation(libs.hikari.cp)
    ksp(libs.akkurate.ksp.plugin)
    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.h2.database)
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
