plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()

    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    linuxArm32Hfp {
        binaries {
            staticLib()
        }
    }

    linuxArm64 {
        binaries {
            staticLib()
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

    macosX64 {
        binaries {
            framework()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":date"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        val linuxArm64Main by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        val mingwX86Main by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":concurrency"))
            }
            kotlin.srcDir("src/nativeMain/kotlin")
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