buildscript {
    val kotlinVersion = "2.0.20"
    repositories {
        maven {
            url = uri("https://maven.google.com/")
        }
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("io.github.gradle-nexus:publish-plugin:1.1.0")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.18.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
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
