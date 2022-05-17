import pw.binom.publish.propertyOrNull

buildscript {

    extra.apply {
        set("kotlin_version", pw.binom.Versions.KOTLIN_VERSION)
        set("network_version", pw.binom.Versions.LIB_VERSION)
        set("serialization_version", "1.0.1")
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://maven.google.com")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${pw.binom.Versions.KOTLIN_VERSION}")
        classpath("com.bmuschko:gradle-docker-plugin:6.6.1")
    }
}

allprojects {
    version = System.getenv("GITHUB_REF_NAME")
        ?: propertyOrNull("version")
            ?.takeIf { it != "unspecified" }
        ?: "1.0.0-SNAPSHOT"
    group = "pw.binom.io"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.binom.pw")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://maven.google.com")
    }
}
