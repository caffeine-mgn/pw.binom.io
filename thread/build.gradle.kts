
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.dependsOn
import pw.binom.useDefault

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
    linuxX64()
    linuxArm64()
    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    mingwX86()
    macosX64()
    macosArm64()
//    allTargets {
//        -"js"
//        -KonanTarget.WASM32
//    }
    eachNative {
        useNative()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":collections"))
                api(project(":concurrency"))
                api(project(":metric"))
                api(project(":io"))
            }
        }
        /*
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val posixMain by creating {
            dependsOn(nativeMain)
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmMain)
            }
        }
        val linuxX64Main by getting {
            dependsOn(posixMain)
        }
        dependsOn("linux*Main", linuxX64Main)
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        dependsOn("mingw*Main", mingwX64Main)

        val macosX64Main by getting {
            dependsOn(posixMain)
        }
        dependsOn("macos*Main", macosX64Main)
*/
        useDefault()
        val jvmMain by getting {
        }
        dependsOn("androidMain", jvmMain)
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
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
