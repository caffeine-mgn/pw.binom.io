import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.eachKotlinTest
import pw.binom.publish.dependsOn
import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    id("com.bmuschko.docker-remote-api")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

fun KotlinNativeTarget.useNativeUtils() {
    compilations["main"].cinterops {
        create("native") {
            defFile = project.file("src/cinterop/native_utils.def")
            packageName = "platform.common"
        }
    }
}

fun KotlinNativeTarget.useNativeMacos() {
    compilations["main"].cinterops {
        create("macos") {
            defFile = project.file("src/cinterop/mac.def")
            packageName = "platform.common"
        }
    }
}

fun KotlinNativeTarget.useNativeMingw() {
    compilations["main"].cinterops {
        create("mingw_epoll") {
            defFile = project.file("src/cinterop/wepoll.def")
            packageName = "platform.common"
        }
        create("mingw") {
            defFile = project.file("src/cinterop/mingw.def")
            packageName = "platform.common"
        }
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
        useNativeUtils()
    }
    linuxArm64 {
        useNativeUtils()
    }
    linuxArm32Hfp {
        useNativeUtils()
    }
    linuxMips32 {
        useNativeUtils()
    }
    linuxMipsel32 {
        useNativeUtils()
    }
    mingwX64 {
        useNativeUtils()
        useNativeMingw()
    }
    mingwX86 {
        useNativeUtils()
        useNativeMingw()
    }
    macosX64 {
        useNativeUtils()
        useNativeMacos()
    }
    macosArm64 {
        useNativeUtils()
        useNativeMacos()
    }
    iosX64 {
        useNativeUtils()
        useNativeMacos()
    }
    iosArm32 {
        useNativeUtils()
        useNativeMacos()
    }
    iosArm64 {
        useNativeUtils()
        useNativeMacos()
    }
    iosSimulatorArm64 {
        useNativeUtils()
        useNativeMacos()
    }
    watchosX64 {
        useNativeUtils()
        useNativeMacos()
    }
    watchosX86 {
        useNativeUtils()
        useNativeMacos()
    }
    watchosArm32 {
        useNativeUtils()
        useNativeMacos()
    }
    watchosArm64 {
        useNativeUtils()
        useNativeMacos()
    }
    watchosSimulatorArm64 {
        useNativeUtils()
        useNativeMacos()
    }
//    androidNativeX64 {
//        useNativeUtils()
//    }
//    androidNativeX86 {
//        useNativeUtils()
//    }
//    androidNativeArm32 {
//        useNativeUtils()
//    }
//    androidNativeArm64 {
//        useNativeUtils()
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":io"))
                api(project(":concurrency"))
            }
        }
        val commonTest by getting {
            dependencies {
                api(project(":thread"))
            }
        }
        val epollLikeMain by creating {

        }
        dependsOn("linuxMain", epollLikeMain)
        dependsOn("mingwMain", epollLikeMain)
        useDefault()
    }
}

//fun makeTimeFile() {
//    val dateDir = file("$buildDir/tmp-date")
//    dateDir.mkdirs()
//    val tzFile = file("$dateDir/currentTZ")
//    tzFile.delete()
//    tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
//}
//
//tasks {
//    withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
//        it.doFirst {
//            makeTimeFile()
//        }
//    }
//}

tasks {
    val httpStorage = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
        image = "ugeek/webdav:amd64",
        tcpPorts = listOf(80 to 7143),
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

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
