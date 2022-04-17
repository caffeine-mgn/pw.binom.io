buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
    }
}

plugins {
    kotlin("jvm") version "1.6.10"
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
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    api("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    api("pw.binom:kn-clang:0.1.1")
    api("com.bmuschko:gradle-docker-plugin:7.3.0")
    api("org.jmailen.gradle:kotlinter-gradle:3.9.0")
    api("com.bnorm.template:kotlin-ir-plugin-gradle:0.1.0-SNAPSHOT")
}
