import pw.binom.publish.useDefault

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
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    macosX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":core"))
                api(project(":date"))
                api(project(":collections"))
//                api("com.ionspin.kotlin:bignum:0.3.4")
            }
        }
        /*
                val linuxX64Main by getting {
                    dependsOn(commonMain)
                }
                if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
                    val linuxArm32HfpMain by getting {
                        dependsOn(commonMain)
                    }
                }
                if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
                    val linuxArm64Main by getting {
                        dependsOn(commonMain)
                    }
                }
                val mingwX64Main by getting {
                    dependsOn(commonMain)
                }
                if (pw.binom.Target.MINGW_X86_SUPPORT) {
                    val mingwX86Main by getting {
                        dependsOn(commonMain)
                    }
                }

                val macosX64Main by getting {
                    dependsOn(commonMain)
                }

                val jvmMain by getting {
                    dependsOn(commonMain)
                }
                if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
                    val androidMain by getting {
                        dependsOn(jvmMain)
                    }
                }
                */
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        /*
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
        */
        useDefault()
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
