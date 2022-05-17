import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
//    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//        id("com.android.library")
//    }
}
// if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    android {
//        compileSdkVersion = "27"
//    }
// }
kotlin {
//    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//        android{
//
//        }
//    }
    jvm()
    linuxX64 {
        binaries {
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/nativeMain/cinterop/core.def")
                    packageName = "pw.binom.internal.core_native"
                    includeDirs.headerFilterOnly("${buildFile.parent}/src/nativeMain/cinterop")
                }
            }
        }
        this.compilations["main"].compileKotlinTask.kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }

    linuxArm32Hfp {
        binaries {
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

    if (pw.binom.Target.MACOS_SUPPORT) {
        macosX64 {
            binaries {
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
    js("js", BOTH) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":io"))
                api(project(":env"))
                api(project(":atomic"))
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
//        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//            val androidMain by getting {
//                dependsOn(jvmMain)
//            }
//        }
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

        if (pw.binom.Target.MACOS_SUPPORT) {
            val macosX64Main by getting {
                dependsOn(nativeMain)
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
//                api(project(":concurrency"))
//                api(project(":file"))
                api(project(":charset"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }

        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }

        val jsTest by getting {
            dependencies {
                api(kotlin("test-js"))
            }
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
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
