import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useNative() {
//    compilations["main"].cinterops {
//        create("native") {
//            defFile = project.file("src/cinterop/native.def")
//            packageName = "platform.common"
//        }
//    }
}

kotlin {
    /*
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()

    */
    allTargets {
        -"js"
        -KonanTarget.WASM32
    }
    eachNative {
        useNative()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":io"))
                api(project(":file"))
                api(project(":env"))
//                api(project(":charset"))
            }
        }

//        val linuxX64Main by getting {
//            dependsOn(commonMain)
//        }
//        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
//            val linuxArm64Main by getting {
//                dependsOn(commonMain)
//                kotlin.srcDir("src/linuxX64Main/kotlin")
//            }
//        }
//        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
//            val linuxArm32HfpMain by getting {
//                dependsOn(commonMain)
//                kotlin.srcDir("src/linuxX64Main/kotlin")
//            }
//        }

//        val mingwX64Main by getting {
//            dependsOn(commonMain)
//        }
//        if (pw.binom.Target.MINGW_X86_SUPPORT) {
//            val mingwX86Main by getting {
//                dependsOn(commonMain)
//                kotlin.srcDir("src/mingwX64Main/kotlin")
//            }
//        }

//        val macosX64Main by getting {
//            dependsOn(commonMain)
//        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":concurrency"))
            }
        }
//        val jvmMain by getting {
//            dependsOn(commonMain)
//            dependencies {}
//        }
//        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//            val androidMain by getting {
//                dependsOn(jvmMain)
//            }
//        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
//        val linuxX64Test by getting {
//            dependsOn(commonTest)
//        }
        useDefault()
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
