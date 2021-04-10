buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    }
}

plugins{
        kotlin("jvm") version "1.4.32"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.4.32")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.32")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    api("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
}