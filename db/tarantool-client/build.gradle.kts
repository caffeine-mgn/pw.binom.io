import org.jetbrains.kotlin.konan.target.HostManager
import pw.binom.eachKotlinTest
import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.bmuschko.docker-remote-api")
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
    linuxX64()
//    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
//        linuxArm32Hfp()
//    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    mingwX64()
//    if (pw.binom.Target.MINGW_X86_SUPPORT) {
//        mingwX86()
//    }
    if (HostManager.hostIsMac) {
        macosX64()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":core"))
                api(project(":db"))
                api(project(":date"))
                api(project(":network"))
                api(project(":ssl"))
            }
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
                api(kotlin("test"))
            }
        }
        useDefault()
    }
}

tasks {
    val tarantool = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
        image = "tarantool/tarantool:2.6.2",
        tcpPorts = listOf(3301 to 7040),
        args = listOf(),
        suffix = "TarantoolServer",
        envs = mapOf(
            "TARANTOOL_USER_NAME" to "server",
            "TARANTOOL_USER_PASSWORD" to "server",
        ),
    )

    eachKotlinTest {
        tarantool.dependsOn(it)
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
