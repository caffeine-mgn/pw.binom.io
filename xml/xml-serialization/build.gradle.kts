import pw.binom.baseStaticLibConfig

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
    linuxArm32Hfp()
    linuxArm64()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
    js("js", BOTH) {
        browser()
        nodejs()
    }
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":xml"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
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
apply<pw.binom.plugins.DocsPlugin>()