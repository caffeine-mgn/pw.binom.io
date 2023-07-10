import pw.binom.publish.getExternalVersion

buildscript {

    extra.apply {
        set("kotlin_version", pw.binom.Versions.KOTLIN_VERSION)
        set("network_version", pw.binom.Versions.LIB_VERSION)
        set("serialization_version", "1.0.1")
    }

    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://maven.google.com")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("com.bmuschko:gradle-docker-plugin:6.6.1")
    }
}

plugins {
//  kotlin("jvm") version "1.8.21" apply false
//  id("org.jetbrains.dokka") version "1.8.10" apply false
//  id("com.gradle.plugin-publish") version "0.16.0" apply false
  id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
}

allprojects {
    version = getExternalVersion()
    group = "pw.binom.io"

    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://maven.google.com")
    }
}

tasks {
    val publishToMavenLocal by creating {
        val self = this
        getTasksByName("publishToMavenLocal", true).forEach {
            if (it !== self) {
                dependsOn(it)
            }
        }
    }

    val publish by creating {
        val self = this
        getTasksByName("publish", true).forEach {
            if (it !== self) {
                dependsOn(it)
            }
        }
    }
}
