import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

kotlin {
    jvm()
    linuxX64()
    linuxArm32Hfp()
    linuxArm64()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
    js("js", BOTH) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
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
        val linuxMips32Main by getting {
            dependsOn(linuxX64Main)
        }

        val linuxMipsel32Main by getting {
            dependsOn(linuxX64Main)
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
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }

        val mingwX64Test by getting {
            dependsOn(linuxX64Test)
        }

        val jsTest by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
