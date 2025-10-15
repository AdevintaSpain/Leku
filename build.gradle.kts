buildscript {
    repositories {
        maven {
            url = uri("https://maven.google.com/")
        }
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.publish.plugin)
        classpath(libs.gradle.nexus.staging.plugin)
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

apply(plugin = "io.github.gradle-nexus.publish-plugin")
apply(from = "$rootDir/scripts/publish-root.gradle")
