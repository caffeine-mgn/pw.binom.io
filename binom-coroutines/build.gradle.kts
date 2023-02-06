import pw.binom.publish.dependsOn
import pw.binom.useDefault

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
//    linuxMips32()
//    linuxMipsel32()
    mingwX64()
//    mingwX86()
    macosX64()
    macosArm64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":thread"))
                api(project(":concurrency"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        useDefault()
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
//        val linuxX64Test by getting {
//            dependsOn(commonTest)
//        }
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
