import pw.binom.baseStaticLibConfig
import java.util.UUID

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()
    linuxX64()
    mingwX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    macosX64()
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":db"))
                api(project(":db:db-serialization-annotations"))
            }
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxX64Main by getting {
                dependsOn(commonMain)
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonMain)
            }
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":db:sqlite"))
                api(project(":network"))
                api(project(":db:postgresql-async"))
                api("pw.binom.io:test-container:${pw.binom.Versions.TEST_CONTAINERS_VERSION}")
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