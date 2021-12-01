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
    linuxArm32Hfp()
    linuxArm64()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    js("js", BOTH) {
        browser()
        nodejs()
    }
    macosX64()
    baseStaticLibConfig()
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
            dependsOn(commonMain)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
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