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
                jvmTarget = "11"
            }
        }
    }

    linuxX64 { // Use your target instead.
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

    mingwX64 { // Use your target instead.
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

    mingwX86 { // Use your target instead.
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
        val mingwX86Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
            kotlin.srcDir("src/nativeMain/kotlin")
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":concurrency"))
                api(project(":file"))
                api(project(":env"))
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

apply<pw.binom.plugins.DocsPlugin>()