import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

val PUBLISH_ARTIFACT_ID by extra("leku")

apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

android {
    resourcePrefix = "leku_"

    defaultConfig {
        minSdk = 23
        compileSdk = 36
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("lib-proguard-rules.txt")
        vectorDrawables.useSupportLibrary = true
    }

    flavorDimensions.add("tier")

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
    packaging {
        resources {
            excludes.add("protobuf.meta")
        }
    }
    lint {
        disable.add("ObsoleteLintCustomCheck")
    }
    publishing {
        singleVariant("release") {}
    }
    namespace = "com.adevinta.leku"
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$rootDir/config/detekt/detekt-config.yml")

    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = true
        sarif.enabled = true
    }
}

ktlint {
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.coroutines.guava)

    val playServicesVersion = "19.0.0"
    implementation("com.google.android.gms:play-services-maps:$playServicesVersion") {
        exclude(group = "com.android.support")
    }
    implementation("com.google.android.gms:play-services-location:21.3.0") {
        exclude(group = "com.android.support")
    }

    implementation("com.google.android.libraries.places:places:4.0.0") {
        exclude(group = "com.android.support")
    }

    implementation(libs.google.maps.services)

    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.mockito.core)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
}
