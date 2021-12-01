plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }

    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
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
                api(project(":date"))
            }
        }
        val commonRunnableMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonRunnableMain)
//            dependencies {
//                api(project(":concurrency"))
//            }
//            kotlin.srcDir("src/nativeMain/kotlin")
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonRunnableMain)
//            dependencies {
//                api(project(":concurrency"))
//            }
//            kotlin.srcDir("src/nativeMain/kotlin")
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonRunnableMain)
//            dependencies {
//                api(project(":concurrency"))
//            }
//            kotlin.srcDir("src/nativeMain/kotlin")
            }
        }

        val mingwX64Main by getting {
            dependsOn(commonRunnableMain)
//            dependencies {
//                api(project(":concurrency"))
//            }
//            kotlin.srcDir("src/nativeMain/kotlin")
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonRunnableMain)
//            dependencies {
//                api(project(":concurrency"))
//            }
//            kotlin.srcDir("src/nativeMain/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(commonRunnableMain)
//            dependencies {
//                api(project(":concurrency"))
//            }
//            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val jvmMain by getting {
            dependsOn(commonRunnableMain)
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