plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    mingwX86()
    macosX64()
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
    wasm32()
    js("js", BOTH) {
        browser()
        nodejs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        val linuxArm64Main by getting {
            dependsOn(linuxX64Main)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(linuxX64Main)
        }

        val mingwX64Main by getting {
            dependsOn(linuxX64Main)
        }
        val mingwX86Main by getting {
            dependsOn(linuxX64Main)
        }

        val macosX64Main by getting {
            dependsOn(linuxX64Main)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
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
apply<pw.binom.plugins.ConfigPublishPlugin>()
