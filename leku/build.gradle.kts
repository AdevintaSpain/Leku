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
    compileSdk = 34
    multiDexEnabled = true
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("lib-proguard-rules.txt")
    vectorDrawables.useSupportLibrary = true
  }

  flavorDimensions("tier")

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
  packaging {
    resources {
      excludes.add("protobuf.meta")
    }
  }
  lint {
    disable.add("ObsoleteLintCustomCheck")
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

  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.fragment:fragment-ktx:1.8.4")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.9.0")

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

  implementation("com.google.maps:google-maps-services:0.2.9")

  val espressoVersion = "3.6.1"
  androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
  androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")

  val supportTestVersion = "1.6.2"
  androidTestImplementation("androidx.test:runner:$supportTestVersion")
  androidTestImplementation("androidx.test:rules:$supportTestVersion")
  androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
  androidTestImplementation("org.mockito:mockito-core:5.14.1")
}