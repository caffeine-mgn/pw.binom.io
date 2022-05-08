import pw.binom.baseStaticLibConfig
import pw.binom.eachKotlinCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

fun androidCInterop(target: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget) {
    target.run {
        binaries {
            compilations["main"].cinterops {
                create("android") {
                    defFile = project.file("src/androidNativeMain/cinterop/android.def")
                    packageName = "platform.android"
                }
            }
        }
    }
}

kotlin {
    jvm()
    linuxX64()
    linuxArm64()
    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
    macosArm64()
    js("js", BOTH) {
        browser()
        nodejs()
    }
    androidNativeX64 {
        androidCInterop(this)
    }
    androidNativeX86 {
        androidCInterop(this)
    }
    androidNativeArm32 {
        androidCInterop(this)
    }
    androidNativeArm64 {
        androidCInterop(this)
    }
    wasm32()
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
            kotlin.srcDir("build/gen")
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxArm64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(nativeMain)
        }
        val linuxMips32Main by getting {
            dependsOn(nativeMain)
        }

        val linuxMipsel32Main by getting {
            dependsOn(nativeMain)
        }

        val androidNativeMain by creating {
            dependsOn(nativeMain)
        }

        val androidNativeX64Main by getting {
            dependsOn(androidNativeMain)
        }
        val androidNativeX86Main by getting {
            dependsOn(androidNativeMain)
        }
        val androidNativeArm32Main by getting {
            dependsOn(androidNativeMain)
        }
        val androidNativeArm64Main by getting {
            dependsOn(androidNativeMain)
        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(nativeMain)
            }
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosArm64Main by getting {
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
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

tasks {
    val generateVersion = create("generateVersion") {
        val sourceDir = project.buildDir.resolve("gen/pw/binom")
        sourceDir.mkdirs()
        val versionSource = sourceDir.resolve("version.kt")
        outputs.files(versionSource)
        inputs.property("version", project.version)

        versionSource.writeText(
            """package pw.binom
            
const val BINOM_VERSION = "${project.version}"
            """
        )
    }
    eachKotlinCompile {
        it.dependsOn(generateVersion)
    }
}
apply<pw.binom.plugins.DocsPlugin>()
