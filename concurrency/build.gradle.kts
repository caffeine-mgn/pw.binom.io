import pw.binom.baseStaticLibConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
            kotlin.srcDir("src/posixMain/kotlin")
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/nativeMain/kotlin")
                kotlin.srcDir("src/posixMain/kotlin")
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/nativeMain/kotlin")
                kotlin.srcDir("src/posixMain/kotlin")
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }
        val linuxMips32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
            kotlin.srcDir("src/posixMain/kotlin")
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val linuxMipsel32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
            kotlin.srcDir("src/posixMain/kotlin")
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val mingwX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/nativeMain/kotlin")
                kotlin.srcDir("src/mingwX64Main/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
            kotlin.srcDir("src/posixMain/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":network"))
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