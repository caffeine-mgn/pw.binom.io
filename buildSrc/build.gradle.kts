buildscript {

  repositories {
    mavenLocal()
    maven(url = "https://repo.binom.pw")
    mavenCentral()
    maven(url = "https://maven.google.com")
  }

  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
    classpath("com.android.tools.build:gradle:7.0.0")
  }
}

plugins {
  kotlin("jvm") version "2.0.20"
  id("com.github.gmazzo.buildconfig") version "3.0.3"
}
val kotlinVersion = kotlin.coreLibrariesVersion
val ionspinBignumVersion = project.property("ionspin_bignum.version") as String
val kotlinxCoroutinesVersion = project.property("kotlinx_coroutines.version") as String
val kotlinxSerializationVersion = project.property("kotlinx_serialization.version") as String
val binomUuidVersion = project.property("binom_uuid.version") as String
val binomAtomicVersion = project.property("binom_atomic.version") as String
val binomBitArrayVersion = project.property("binom_bitarray.version") as String
val httpKotlinPluginGradle = project.property("http-kotlin-plugin-gradle") as String
val binomUrlVersion = project.property("binom_url.version") as String


buildConfig {
  packageName(project.group.toString())
  buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
  buildConfigField("String", "IONSPIN_BIGNUM_VERSION", "\"$ionspinBignumVersion\"")
  buildConfigField("String", "KOTLINX_COROUTINES_VERSION", "\"$kotlinxCoroutinesVersion\"")
  buildConfigField("String", "KOTLINX_SERIALIZATION_VERSION", "\"$kotlinxSerializationVersion\"")
  buildConfigField("String", "BINOM_UUID_VERSION", "\"$binomUuidVersion\"")
  buildConfigField("String", "BINOM_HTTP_PLUGIN", "\"$httpKotlinPluginGradle\"")
  buildConfigField("String", "BITARRAY_VERSION", "\"$binomBitArrayVersion\"")
  buildConfigField("String", "BINOM_URL_VERSION", "\"$binomUrlVersion\"")
  buildConfigField("String", "ATOMIC_VERSION", "\"$binomAtomicVersion\"")
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
  api("pw.binom:kn-clang:1.0.0-SNAPSHOT")
  api("com.bmuschko:gradle-docker-plugin:7.3.0")
//  api("pw.binom:binom-publish:0.1.19")
  api("pw.binom:binom-publish:0.1.23")
//  api("com.jakewharton.cite:cite-gradle-plugin:0.2.0")
//    api("com.android.library:com.android.library.gradle.plugin:7.2.0")
  api("com.android.tools.build:gradle:4.2.1")
  api("com.google.gms:google-services:4.3.5")
  api("org.jmailen.gradle:kotlinter-gradle:3.14.0")
}
