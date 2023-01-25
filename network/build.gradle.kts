import pw.binom.eachKotlinTest

plugins {
    id("org.jetbrains.kotlin.multiplatform")
//    id("com.bnorm.template.kotlin-ir-plugin")
    id("maven-publish")
    id("com.bmuschko.docker-remote-api")
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
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }

    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }

    macosX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":env"))
                api(project(":concurrency"))
                api(project(":thread"))
                api(project(":collections"))
//                api(project(":nio"))
                api(project(":socket"))
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
                api(project(":coroutines"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmLikeMain by creating {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(jvmLikeMain)
            dependencies {}
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
//                implementation("junit:junit:4.13.2")
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
            }
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmLikeMain)
            }
            val androidTest by getting {
                dependsOn(commonTest)
                dependencies {
                    implementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
                    implementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
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

    val httpStorage = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
        image = "ugeek/webdav:amd64",
        tcpPorts = listOf(80 to 7141),
        args = listOf(),
        suffix = "WebDav",
        envs = mapOf(
            "USERNAME" to "root",
            "PASSWORD" to "root",
            "TZ" to "GMT",
        )
    )

    eachKotlinTest {
        httpStorage.dependsOn(it)
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
