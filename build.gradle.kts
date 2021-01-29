buildscript {
    val kotlin_version = "1.4.21"
    extra.apply {
        set("kotlin_version", kotlin_version)
        set("network_version", "0.1.26")
        set("serialization_version", "1.0.1")
    }

    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
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