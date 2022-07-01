buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://maven.google.com")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath("com.android.tools.build:gradle:7.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}
val kotlinVersion = project.property("kotlin.version") as String

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
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
    api("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    api("pw.binom:kn-clang:0.1.1")
    api("com.bmuschko:gradle-docker-plugin:7.3.0")
    api("pw.binom:binom-publish:0.1.2")
//    api("com.android.library:com.android.library.gradle.plugin:7.2.0")
    api("com.android.tools.build:gradle:4.2.1")
    api("com.google.gms:google-services:4.3.5")
}
