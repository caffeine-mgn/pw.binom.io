import pw.binom.baseStaticLibConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
//    id("com.bnorm.template.kotlin-ir-plugin")
}

//template {
//    companionProcessing.set(false)
//    valueClassProcessing.set(false)
//}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
//    linuxMips32()
//    linuxMipsel32()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val nativeMain by creating{
            dependsOn(commonMain)
        }
        val posixMain by creating{
            dependsOn(nativeMain)
        }
        val jvmMain by getting {
//            dependencies {
//                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
//            }
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(posixMain)
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(linuxX64Main)
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(linuxX64Main)
            }
        }
//        val linuxMips32Main by getting {
//            dependsOn(linuxX64Main)
//        }
//
//        val linuxMipsel32Main by getting {
//            dependsOn(linuxX64Main)
//        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(mingwX64Main)
            }
        }

        val macosX64Main by getting {
            dependsOn(posixMain)
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
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}
apply<pw.binom.plugins.DocsPlugin>()