buildscript {

  repositories {
    mavenLocal()
    maven(url = "https://repo.binom.pw")
    mavenCentral()
    maven(url = "https://maven.google.com")
  }

  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    classpath("com.android.tools.build:gradle:7.0.0")
  }
}

plugins {
  kotlin("jvm") version "2.1.0"
  id("com.github.gmazzo.buildconfig") version "5.5.1"
}

val kotlinVersion = kotlin.coreLibrariesVersion
val httpKotlinPluginGradle = project.property("http-kotlin-plugin-gradle") as String


buildConfig {
  packageName(project.group.toString())
  buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
  buildConfigField("String", "BINOM_HTTP_PLUGIN", "\"$httpKotlinPluginGradle\"")
}

repositories {
  mavenLocal()
  mavenCentral()
  maven(url = "https://repo.binom.pw")
  maven(url = "https://plugins.gradle.org/m2/")
  maven(url = "https://maven.google.com")
}

dependencies {
  api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
  api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
  api("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
  api("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
  api("pw.binom:kn-clang:0.1.19")
  api("com.bmuschko:gradle-docker-plugin:7.3.0")
//  api("pw.binom:binom-publish:0.1.19")
  api("pw.binom:binom-publish:0.1.23")
//  api("com.jakewharton.cite:cite-gradle-plugin:0.2.0")
//    api("com.android.library:com.android.library.gradle.plugin:7.2.0")
  api("com.android.tools.build:gradle:8.6.1")
  api("com.google.gms:google-services:4.3.5")
  api("org.jmailen.gradle:kotlinter-gradle:3.14.0")
}
