import pw.binom.eachKotlinCompile
import pw.binom.publish.dependsOn

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
fun androidCInterop(target: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget) {
//    target.run {
//        binaries {
//            compilations["main"].cinterops {
//                create("android") {
//                    defFile = project.file("src/androidNativeMain/cinterop/android.def")
//                    packageName = "platform.android"
//                }
//            }
//        }
//    }
}
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
    js(pw.binom.Target.JS_TARGET) {
        browser()
        nodejs()
    }
    androidNativeX64 {
        androidCInterop(this)
    }
    androidNativeX86 {
        androidCInterop(this)
    }
    androidNativeArm32 {
        androidCInterop(this)
    }
    androidNativeArm64 {
        androidCInterop(this)
    }
    wasm32()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib"))
                api(project(":collections"))
            }
            kotlin.srcDir("build/gen")
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        dependsOn("linux*Main", linuxX64Main)
        dependsOn("wasm*Main", nativeMain)
//        dependsOn("ios*Main", nativeMain)

        val androidNativeMain by creating {
            dependsOn(nativeMain)
        }
        dependsOn("androidNative*Main", androidNativeMain)
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        dependsOn("mingw*Main", mingwX64Main)
        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        dependsOn("macos*Main", macosX64Main)

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

tasks {
    val generateVersion = create("generateVersion") {
        val sourceDir = project.buildDir.resolve("gen/pw/binom")
        sourceDir.mkdirs()
        val versionSource = sourceDir.resolve("version.kt")
        outputs.files(versionSource)
        inputs.property("version", project.version)

        versionSource.writeText(
            """package pw.binom

const val BINOM_VERSION = "${project.version}"
"""
        )
    }
    eachKotlinCompile {
        it.dependsOn(generateVersion)
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
