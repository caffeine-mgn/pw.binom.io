plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            binaries {
                staticLib()
            }
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 { // Use your target instead.
            binaries {
                staticLib()
            }
        }
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64 {
            binaries {
                staticLib()
            }
        }
    }

    macosX64 {
        binaries {
            framework()
        }
    }
    js("js", BOTH) {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":logger"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
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

        val mingwX64Main by getting {
            dependsOn(linuxX64Main)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(linuxX64Main)
            }
        }

        val macosX64Main by getting {
            dependsOn(linuxX64Main)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
        val jsTest by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }

    }
}
apply<pw.binom.plugins.DocsPlugin>()