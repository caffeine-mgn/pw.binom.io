plugins {
    id("org.jetbrains.kotlin.multiplatform")
//    id("com.bnorm.template.kotlin-ir-plugin")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
    jvm()
    linuxX64 {
        binaries {
            compilations["main"].cinterops {
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            binaries {
                compilations["main"].cinterops {
                    create("native") {
                        defFile = project.file("src/cinterop/native.def")
                        packageName = "platform.linux"
                    }
                }
            }
        }
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64 {
            binaries {
                compilations["main"].cinterops {
                    create("native") {
                        defFile = project.file("src/cinterop/native.def")
                        packageName = "platform.linux"
                    }
                }
            }
        }
    }

    mingwX64 {
        binaries {
            compilations["main"].cinterops {
                create("wepoll") {
                    defFile = project.file("src/cinterop/wepoll.def")
                    packageName = "platform.linux"
                }
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 {
            binaries {
                compilations["main"].cinterops {
                    create("wepoll") {
                        defFile = project.file("src/cinterop/wepoll.def")
                        packageName = "platform.linux"
                    }
                    create("native") {
                        defFile = project.file("src/cinterop/native.def")
                        packageName = "platform.linux"
                    }
                }
            }
        }
    }

    macosX64 {
        binaries {
            compilations["main"].cinterops {
                create("utils") {
                    defFile = project.file("src/cinterop/mac.def")
                    packageName = "platform.linux"
                }
                create("native") {
                    defFile = project.file("src/cinterop/native.def")
                    packageName = "platform.linux"
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":env"))
                api(project(":concurrency"))
                api(project(":thread"))
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(nativeMain)
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(nativeMain)
                kotlin.srcDir("src/linuxX64Main/kotlin")
            }
        }

        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(nativeMain)
                kotlin.srcDir("src/mingwX64Main/kotlin")
            }
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":date"))
                api(project(":charset"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {}
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmMain)
            }
            val androidTest by getting {
                dependsOn(commonTest)
                dependencies {
//                    api(kotlin("test"))
                    api(kotlin("test-junit"))
//                    api(kotlin("test-common"))
//                    api(kotlin("test-annotations-common"))
                    api("com.android.support.test:runner:0.5")
                }
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}
tasks {
    withType(Test::class) {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
        testLogging.showCauses = true
        testLogging.showExceptions = true
        testLogging.showStackTraces = true
        testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
