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
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.binom.pw")
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.20")
    api("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    api("pw.binom:kn-clang:0.1.1")
    api("com.bmuschko:gradle-docker-plugin:7.3.0")
    api("org.jmailen.gradle:kotlinter-gradle:3.9.0")
}
