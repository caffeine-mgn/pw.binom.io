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
    jvm()
    linuxArm32Hfp {
        binaries {
            staticLib {
            }
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("openssl") {
                    defFile = project.file("src/mingwX64Main/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("openssl") {
                    defFile = project.file("src/mingwX64Main/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
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
                create("utils") {
                    defFile = project.file("src/cinterop/mac.def")
                    packageName = "platform.linux"
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))

                api(project(":core"))
                api(project(":network"))
                api(project(":file"))
                api(project(":date"))
                api(project(":concurrency"))
            }
        }
        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(mingwX64Main)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(linuxX64Main)
        }


        val mingwX86Main by getting {
            dependsOn(mingwX64Main)
        }

        val macosX64Main by getting {
            dependsOn(mingwX64Main)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.bouncycastle:bcprov-jdk15on:1.61")
                api("org.bouncycastle:bcpkix-jdk15on:1.61")
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