import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    /*
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    macosX64()
    */
    targets.all {
        compilations["main"].compileKotlinTask.kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
    allTargets {
        -"js"
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":httpServer"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
        }
        /*
        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
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
*/
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        /*
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
        */
        useDefault()
    }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
