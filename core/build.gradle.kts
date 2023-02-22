import pw.binom.publish.dependsOn
import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useNative() {
    compilations["main"].cinterops {
        create("native") {
            defFile = project.file("src/cinterop/native.def")
            packageName = "platform.common"
        }
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
    linuxX64 {
        useNative()
    }
    linuxArm64 {
        useNative()
    }
    linuxArm32Hfp {
        useNative()
    }
    linuxMips32 {
        useNative()
    }
    linuxMipsel32 {
        useNative()
    }
    mingwX64 {
        useNative()
    }
    mingwX86 {
        useNative()
    }
    macosX64 {
        useNative()
    }
    macosArm64 {
        useNative()
    }
    iosX64 {
        useNative()
    }
    iosArm32 {
        useNative()
    }
    iosArm64 {
        useNative()
    }
    iosSimulatorArm64 {
        useNative()
    }
    watchosX64 {
        useNative()
    }
    watchosX86 {
        useNative()
    }
    watchosArm32 {
        useNative()
    }
    watchosArm64 {
        useNative()
    }
    watchosSimulatorArm64 {
        useNative()
    }
    androidNativeX64 {
        useNative()
    }
    androidNativeX86 {
        useNative()
    }
    androidNativeArm32 {
        useNative()
    }
    androidNativeArm64 {
        useNative()
    }
    wasm32()
    js(pw.binom.Target.JS_TARGET) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":io"))
                api(project(":env"))
                api(project(":atomic"))
                api(project(":collections"))
                api(project(":pool"))
                api(project(":url"))
                api(project(":uuid"))
            }
        }
        val jvmLikeMain by creating {
            dependsOn(commonMain)
        }
        dependsOn("androidMain", jvmLikeMain)
        val jvmMain by getting {
            dependsOn(jvmLikeMain)
        }
//        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//            val androidMain by getting {
//                dependsOn(jvmMain)
//            }
//        }
        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        val nativeHostedMain by creating {
            dependsOn(nativeCommonMain)
        }

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
                api(project(":charset"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
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
