plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    this.metadata {
    }
    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("openssl") {
                    defFile = project.file("src/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
            val args = listOf(
                "-include-binary", "${buildFile.parent}/src/linuxX64Main/cinterop/lib/libopenssl.a"
            )
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
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
                create("openssl") {
                    defFile = project.file("src/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
            val args = listOf(
                "-include-binary", "${buildFile.parent}/src/linuxArm32HfpMain/cinterop/lib/libopenssl.a"
            )
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("openssl") {
                    defFile = project.file("src/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
            val args = listOf(
                "-include-binary", "${buildFile.parent}/src/mingwX64Main/cinterop/lib/libopenssl.a"
            )
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("openssl") {
                    defFile = project.file("src/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
            val args = listOf(
                "-include-binary", "${buildFile.parent}/src/mingwX86Main/cinterop/lib/libopenssl.a"
            )
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
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
            framework()
            staticLib()
            compilations["main"].cinterops {
                create("openssl") {
                    defFile = project.file("src/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
                }
            }
            val args = listOf(
                "-include-binary", "${buildFile.parent}/src/macosX64Main/cinterop/lib/libopenssl.a"
            )
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
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
//            dependsOn(mingwX64Main)
            dependsOn(commonMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
        }
        val linuxArm32HfpMain by getting {
//            dependsOn(linuxX64Main)
            dependsOn(commonMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
        }


        val mingwX86Main by getting {
//            dependsOn(mingwX64Main)
            dependsOn(commonMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
        }

        val macosX64Main by getting {
//            dependsOn(mingwX64Main)
            dependsOn(commonMain)
            kotlin.srcDir("src/mingwX64Main/kotlin")
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
apply<pw.binom.plugins.DocsPlugin>()