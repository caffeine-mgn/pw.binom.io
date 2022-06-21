import pw.binom.eachKotlinCompile
import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

kotlin {
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
    jvm()
    linuxX64()
    linuxArm32Hfp()
    linuxArm64()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
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

        val implMain by creating {
            dependsOn(commonMain)
        }

        val jsMain by getting {
            dependsOn(implMain)
        }

        val linuxX64Main by getting {
            dependsOn(implMain)
        }
        val linuxArm64Main by getting {
            dependsOn(linuxX64Main)
        }
        val linuxArm32HfpMain by getting {
            dependsOn(linuxX64Main)
        }

        val linuxMips32Main by getting {
            dependsOn(linuxX64Main)
        }

        val linuxMipsel32Main by getting {
            dependsOn(linuxX64Main)
        }

        val mingwX64Main by getting {
            dependsOn(implMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(mingwX64Main)
            }
        }

        val macosX64Main by getting {
            dependsOn(implMain)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":env"))
            }
            kotlin.srcDir("build/gen")
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmMain)
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
