import pw.binom.publish.dependsOn
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
//    linuxMips32()
//    linuxMipsel32()
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
//    androidNativeX64()
//    androidNativeX86()
//    androidNativeArm32()
//    androidNativeArm64()
//    wasm32()
    js("js", pw.binom.Target.JS_TARGET) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":uuid"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
        }
        val jvmLikeMain by creating {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(jvmLikeMain)
        }
        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        val nativeHostedMain by creating {
            dependsOn(nativeCommonMain)
        }

        dependsOn("androidMain", jvmLikeMain)
        dependsOn("linux*Main", nativeHostedMain)
        dependsOn("mingw*Main", nativeHostedMain)
        dependsOn("watchos*Main", nativeHostedMain)
        dependsOn("macos*Main", nativeHostedMain)
        dependsOn("ios*Main", nativeHostedMain)
        dependsOn("androidNative*Main", nativeHostedMain)
        dependsOn("wasm*Main", nativeCommonMain)

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }

        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }

        val jsTest by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }
    }
}

fun makeTimeFile() {
    val dateDir = file("$buildDir/tmp-date")
    dateDir.mkdirs()
    val tzFile = file("$dateDir/currentTZ")
    tzFile.delete()
    tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
}

tasks {
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
        it.doFirst {
            makeTimeFile()
        }
    }
}
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
