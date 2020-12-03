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
    js {
        browser()
        nodejs()
    }
    linuxArm32Hfp {
        binaries {
            staticLib {
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

    linuxArm64 {
        binaries {
            staticLib {
            }
        }
    }
    macosX64 {
        binaries {
            framework {
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val jsMain by getting {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(linuxX64Main)
        }

        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX86Main by getting {
            dependsOn(mingwX64Main)
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