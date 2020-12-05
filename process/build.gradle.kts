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
            compilations["main"].cinterops {
                create("signals") {
                    defFile = project.file("src/cinterop/linux.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    jvm()
    linuxArm32Hfp {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("signals") {
                    defFile = project.file("src/cinterop/linux.def")
                    packageName = "platform.linux"
                }
            }
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

//    linuxArm64 {
//        binaries {
//            staticLib {
//            }
//        }
//    }
    macosX64 {
        binaries {
            framework {
            }
            compilations["main"].cinterops {
                create("signals") {
                    defFile = project.file("src/cinterop/linux.def")
                    packageName = "platform.linux"
                }
            }
        }
    }

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
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        val mingwX86Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
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