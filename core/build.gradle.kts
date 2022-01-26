import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jmailen.kotlinter")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()

    linuxX64 {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
    }

    linuxArm32Hfp {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
    }

    linuxArm64 {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
    }

    linuxMips32 {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
    }

    linuxMipsel32 {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
    }

    mingwX64 {
        binaries {
            staticLib()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 {
            binaries {
                staticLib()
                compilations["main"].cinterops {
                    create("native") {
                        defFile = project.file("src/nativeMain/cinterop/core.def")
                        packageName = "pw.binom.internal.core_native"
                        includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                    }
                }
            }
        }
    }

    macosX64 {
        binaries {
            framework()
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
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
                api(project(":env"))
            }
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

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
//                api(project(":concurrency"))
//                api(project(":file"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
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

fun makeTimeFile() {
    val dateDir = file("$buildDir/tmp-date")
    dateDir.mkdirs()
    val tzFile = file("$dateDir/currentTZ")
    tzFile.delete()
    tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
}

tasks {
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
        it.doFirst {
            makeTimeFile()
        }
    }
}

kotlinter {
    indentSize = 4
    disabledRules = arrayOf("no-wildcard-imports")
}

apply<pw.binom.plugins.DocsPlugin>()
