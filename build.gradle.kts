buildscript {

    extra.apply {
        set("kotlin_version", pw.binom.Versions.KOTLIN_VERSION)
        set("network_version", pw.binom.Versions.LIB_VERSION)
        set("serialization_version", "1.0.1")
    }

    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("com.bmuschko:gradle-docker-plugin:6.6.1")
    }
}

allprojects {
    version = pw.binom.Versions.LIB_VERSION
    group = "pw.binom.io"

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}