import pw.binom.eachKotlinCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }

    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    linuxArm32Hfp {
        binaries {
            staticLib()
        }
    }

    linuxArm64 {
        binaries {
            staticLib()
        }
    }

    linuxMips32 {
        binaries {
            staticLib()
        }
    }

    linuxMipsel32 {
        binaries {
            staticLib()
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 { // Use your target instead.
            binaries {
                staticLib()
            }
        }
    }

    macosX64 {
        binaries {
            framework()
        }
    }
    js("js", BOTH) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
            kotlin.srcDir("build/gen")
        }
        val linuxX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        val linuxArm64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        val linuxMips32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val linuxMipsel32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/nativeMain/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
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

tasks {
    val generateVersion = create("generateVersion") {
        val sourceDir = project.buildDir.resolve("gen/pw/binom")
        sourceDir.mkdirs()
        val versionSource = sourceDir.resolve("version.kt")
        outputs.files(versionSource)
        inputs.property("version", project.version)

        versionSource.writeText(
            """package pw.binom
            
const val BINOM_VERSION = "${project.version}""""
        )
    }
    eachKotlinCompile {
        it.dependsOn(generateVersion)
    }
}
apply<pw.binom.plugins.DocsPlugin>()