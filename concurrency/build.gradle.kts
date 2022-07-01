import pw.binom.publish.dependsOn

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
//    linuxArm64()
//    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
//    mingwX86()
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val posixMain by creating {
            dependsOn(nativeMain)
        }

        val linuxX64Main by getting {
            dependsOn(posixMain)
        }
        val macosX64Main by getting {
            dependsOn(posixMain)
        }
        dependsOn("linux*Main", linuxX64Main)
        dependsOn("mingw*Main", nativeMain)
        dependsOn("watchos*Main", macosX64Main)
        dependsOn("macos*Main", macosX64Main)
        dependsOn("ios*Main", macosX64Main)
        dependsOn("androidNative*Main", linuxX64Main)

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {}
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmMain)
            }
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":network"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
