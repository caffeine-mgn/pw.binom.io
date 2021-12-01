import pw.binom.eachKotlinCompile
import java.util.*

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
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val linuxArm64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val linuxMips32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val linuxMipsel32Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/mingwX64Main/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/linuxX64Main/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":env"))
            }
            kotlin.srcDir("build/gen")
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

        val jsTest by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }
    }
}

tasks {
    fun generateDate() {
        val sourceDir = project.buildDir.resolve("gen/pw/binom/date")
        sourceDir.mkdirs()
        val versionSource = sourceDir.resolve("test_data.kt")
        versionSource.writeText(
            """package pw.binom.date
            
val test_data_currentTZ get() = ${TimeZone.getDefault().rawOffset.let { it / 1000 / 60 }}
val test_data_now get() = ${Date().time}
"""
        )
    }


    eachKotlinCompile {
        it.doFirst {
            generateDate()
        }
    }
}
apply<pw.binom.plugins.DocsPlugin>()