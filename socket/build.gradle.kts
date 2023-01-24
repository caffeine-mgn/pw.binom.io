import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.eachKotlinTest
import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
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

fun KotlinNativeTarget.useNativeNet() {
    val headersPath = project.buildFile.parentFile.resolve("src/native/include")
    val staticBuildTask = clangBuildStatic(name = "binom-socket", target = konanTarget) {
        include(headersPath)
        compileFile(file("${buildFile.parentFile}/src/native/src/Event.c"))
        compileFile(file("${buildFile.parentFile}/src/native/src/SelectedList.c"))
        compileFile(file("${buildFile.parentFile}/src/native/src/Selector.c"))
    }
    tasks.findByName(compileTaskName)?.dependsOn(staticBuildTask)
    val args = listOf("-include-binary", staticBuildTask.staticFile.asFile.get().absolutePath)
    compilations["main"].kotlinOptions.freeCompilerArgs = args
    compilations["test"].kotlinOptions.freeCompilerArgs = args
    compilations["main"].cinterops {
        create("nativeCommon") {
            defFile = project.file("src/cinterop/native.def")
            packageName = "platform.common"
            includeDirs.headerFilterOnly(headersPath)
        }
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

// fun KotlinNativeTarget.useNativeWepoll() {
//    compilations["main"].cinterops {
//        create("epoll") {
//            defFile = project.file("src/cinterop/wepoll.def")
//            packageName = "platform.common"
//        }
//    }
// }

fun KotlinNativeTarget.useNativeMingw() {
    compilations["main"].cinterops {
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
//        useNativeWepoll()
        useNativeNet()
    }
    linuxArm64 {
        useNativeUtils()
//        useNativeWepoll()
        useNativeNet()
    }
    linuxArm32Hfp {
        useNativeUtils()
//        useNativeWepoll()
        useNativeNet()
    }
    linuxMips32 {
        useNativeUtils()
//        useNativeWepoll()
        useNativeNet()
    }
    linuxMipsel32 {
        useNativeUtils()
//        useNativeWepoll()
        useNativeNet()
    }
    mingwX64 {
        useNativeUtils()
        useNativeMingw()
//        useNativeWepoll()
        useNativeNet()
    }
    mingwX86 {
        useNativeUtils()
        useNativeMingw()
//        useNativeWepoll()
        useNativeNet()
    }
    macosX64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    macosArm64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    iosX64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    iosArm32 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    iosArm64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    iosSimulatorArm64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    watchosX64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    watchosX86 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    watchosArm32 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    watchosArm64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
    }
    watchosSimulatorArm64 {
        useNativeUtils()
        useNativeMacos()
        useNativeNet()
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
        useDefault()
        val nativeRunnableMain by getting {
        }
        val nativeRunnableTest by getting {
        }
        val epollLikeMain by creating {
            dependsOn(nativeRunnableMain)
        }
        val epollLikeTest by creating {
            dependsOn(nativeRunnableTest)
        }
        dependsOn("linuxMain", epollLikeMain)
        dependsOn("mingwMain", epollLikeMain)
        dependsOn("linuxTest", epollLikeTest)
        dependsOn("mingwTest", epollLikeTest)
    }
}

// fun makeTimeFile() {
//    val dateDir = file("$buildDir/tmp-date")
//    dateDir.mkdirs()
//    val tzFile = file("$dateDir/currentTZ")
//    tzFile.delete()
//    tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
// }
//
// tasks {
//    withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
//        it.doFirst {
//            makeTimeFile()
//        }
//    }
// }

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
