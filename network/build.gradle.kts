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
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }
    linuxArm32Hfp {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    linuxArm64 {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
//    linuxArm64 {
//        binaries {
//            staticLib()
//        }
//    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("wepoll") {
                    defFile = project.file("src/cinterop/wepoll.def")
                    packageName = "platform.linux"
                }
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("wepoll") {
                    defFile = project.file("src/cinterop/wepoll.def")
                    packageName = "platform.linux"
                }
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }

    macosX64 {
        binaries {
            framework {
            }
            compilations["main"].cinterops {
                create("utils") {
                    defFile = project.file("src/cinterop/mac.def")
                    packageName = "platform.linux"
                }
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":env"))
                api(project(":concurrency"))
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(nativeMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val linuxArm64Main by getting {
            dependsOn(nativeMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX86Main by getting {
            dependsOn(nativeMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
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