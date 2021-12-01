import pw.binom.baseStaticLibConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()
    linuxX64 {
        binaries {
            compilations["main"].cinterops {
                create("signals") {
                    defFile = project.file("src/cinterop/linux.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            binaries {
                compilations["main"].cinterops {
                    create("signals") {
                        defFile = project.file("src/cinterop/linux.def")
                        packageName = "platform.linux"
                    }
                }
            }
        }
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64 {
            binaries {
                compilations["main"].cinterops {
                    create("signals") {
                        defFile = project.file("src/cinterop/linux.def")
                        packageName = "platform.linux"
                    }
                }
            }
        }
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64 {
        binaries {
            compilations["main"].cinterops {
                create("signals") {
                    defFile = project.file("src/cinterop/linux.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":concurrency"))
                api(kotlin("stdlib-common"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }
        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/mingwX64Main/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
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
    }
}
apply<pw.binom.plugins.DocsPlugin>()