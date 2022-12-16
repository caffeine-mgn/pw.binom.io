import pw.binom.*
import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
    jvm()
    linuxX64()
    linuxArm64()
    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    mingwX86()
    macosX64()
    macosArm64()
    iosX64()
    iosArm32()
    iosArm64()
    iosSimulatorArm64()
    watchosX64()
    watchosX86()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":io"))
//                api(project(":atomic"))
            }
        }
        useDefault()
    }
}

//fun makeTimeFile() {
//    val dateDir = file("$buildDir/tmp-date")
//    dateDir.mkdirs()
//    val tzFile = file("$dateDir/currentTZ")
//    tzFile.delete()
//    tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
//}
//
//tasks {
//    withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
//        it.doFirst {
//            makeTimeFile()
//        }
//    }
//}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
