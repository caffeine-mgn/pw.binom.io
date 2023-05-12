plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useLinux() {
    compilations["main"].cinterops {
        create("linux") {
            defFile = project.file("src/cinterop/linux.def")
            packageName = "platform.common"
        }
    }
}
kotlin {
    jvm()
    linuxX64 {
        useLinux()
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            useLinux()
        }
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64 {
            useLinux()
        }
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64 {
        useLinux()
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
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(linuxX64Main)
            }
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(linuxX64Main)
            }
        }
        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(mingwX64Main)
            }
        }

        val macosX64Main by getting {
            dependsOn(linuxX64Main)
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
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
