import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import pw.binom.BuildBluezTask
import pw.binom.ClearKLibCacheTask
import pw.binom.DownloadTask
import pw.binom.ExtractTask
import pw.binom.kotlin.clang.*
import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
  id("com.github.gmazzo.buildconfig")
//  id("com.jakewharton.cite")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
buildConfig {
  packageName("pw.binom.bluetooth")
  className("BuildConfig")
  buildConfigField("String", "PROJECT_VERSION", "\"${version}\"")
}
//fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useNative() {
//  compilations["main"].cinterops {
//    create("native") {
//      definitionFile.set(project.file("src/cinterop/native.def"))
//      packageName = "platform.common"
//    }
//  }
//}

apply<pw.binom.KotlinConfigPlugin>()
fun KotlinNativeTarget.bluez(): Pair<ExtractTask, BuildBluezTask> {
  val extractBluez = tasks.register("extract${konanTarget.name}", ExtractTask::class) {
    dependsOn(downloadBluez)
    input = downloadBluez.output
    output = project.layout.buildDirectory.file("bluez/${konanTarget.name}")
  }
  val buildBluez = project.tasks.register("buildBluez${this.konanTarget.name}", BuildBluezTask::class.java) {
    group = "clang"
    target = konanTarget
    bluezDirection.set(extractBluez.get().output)
  }
  return extractBluez.get() to buildBluez.get()
}

val headersPath = project.buildFile.parentFile.resolve("src/native/include")

fun KotlinNativeTarget.useNativeBluetooth() {
//  val bluezTasks = if (konanTarget.family == Family.LINUX) {
//    bluez()
//  } else {
//    null
//  }

//  val extractBluez = bluezTasks?.first
//  val buildBluez = bluezTasks?.second
  val buildBluez = tasks.withType<BuildBluezTask>().find { it.target.get() == konanTarget }
  val staticLib = buildBluez?.bluezDirection?.get()?.asFile?.resolve("lib/.libs/libbluetooth.a")
  fun BuildTask.configBuild() {
    if (buildBluez != null) {
      dependsOn(buildBluez)
    }

    group = "clang"
    konanVersion.set(pw.binom.Versions.KOTLIN_VERSION)
    val list = ArrayList<String>()
    list += "-I"
    list += headersPath.absolutePath
    if (buildBluez != null) {
      list += "-I"
      list += buildBluez.bluezDirection.get().asFile.resolve("lib").absolutePath
    }
    compileArgs(*list.toTypedArray())
    compileFile(file("${buildFile.parentFile}/src/native/cpp/devices.cpp"))
    compileFile(file("${buildFile.parentFile}/src/native/cpp/spp.cpp"))
    compileFile(file("${buildFile.parentFile}/src/native/cpp/services.cpp"))
    compileFile(file("${buildFile.parentFile}/src/native/cpp/utils.cpp"))
  }

  val staticBuildTask =
    clangBuildStatic(name = "binom-bluetooth", target = konanTarget) {
      configBuild()
    }
  val dynamicTask = clangBuildDynamic(name = "binom-bluetooth", target = konanTarget) {
    configBuild()
    compileArgs("-DBUILD_SHARED_LIB", "-fPIC")
    linkArgs("-lstdc++", "-fPIC")
    if (target.get().family == Family.MINGW) {
      linkArgs("-lbthprops", "-lws2_32")
    }
    if (staticLib != null) {
      linkArgs(
        "-L${staticLib.parentFile.absolutePath}",
        "-l${staticLib.nameWithoutExtension.removePrefix("lib")}",
      )
    }
//
  }
  val targetName = compileTaskName.removePrefix("compileKotlin")
  val removeCacheTask = tasks.register("deleteCache$targetName", ClearKLibCacheTask::class) {
    this.target = konanTarget
  }
  staticBuildTask.dependsOn(removeCacheTask)
  val list = ArrayList<String>()
  list += "-include-binary"
  list += staticBuildTask.staticFile.asFile.get().absolutePath

  if (staticLib != null) {
    list += "-include-binary"
    list += staticLib.absolutePath
  }
  compilations["main"].apply {
    this.compileTaskProvider.get().apply {
      if (buildBluez != null) {
        dependsOn(buildBluez)
      }
      dependsOn(staticBuildTask)
      compilerOptions.freeCompilerArgs = list
      doFirst {
        println("Compile ARGS: $this ----> $list")
      }
    }
  }
  compilations["main"].apply {
    cinterops {
      create("nativeCommon") {
        definitionFile.set(project.file("src/native/native.def"))
        packageName = "platform.bluetooth"
        includeDirs.headerFilterOnly(headersPath)
      }
    }
  }
}

val downloadBluez by tasks.creating(DownloadTask::class) {
  output.set(project.layout.buildDirectory.file("bluez/bluez.zip"))
  url = "https://github.com/bluez/bluez/archive/refs/tags/5.79.zip"
}

kotlin {
  allTargets {
    config()
    -"iosArm64"
    -"iosSimulatorArm64"
    -"iosX64"
    -"macosArm64"
    -"macosX64"
    -"tvosArm64"
    -"tvosSimulatorArm64"
    -"watchosArm32"
    -"watchosArm64"
    -"watchosDeviceArm64"
    -"watchosSimulatorArm64"
    -"watchosX64"

    -"js"
    -"android"
    -"androidNativeArm32"
    -"androidNativeArm64"
    -"androidNativeX64"
    -"androidNativeX86"
    -"wasmJs"
    -"wasmWasi"
  }
  eachNative {
    if (this.konanTarget.family != Family.LINUX) {
      return@eachNative
    }
    bluez()
  }
  eachNative {
    if (this.konanTarget.family != Family.LINUX && this.konanTarget.family != Family.MINGW) {
      return@eachNative
    }
    useNativeBluetooth()

//    compilations["main"].cinterops {
//      val bluez by creating {
//        definitionFile.set(project.file("src/native/native.def"))
//        packageName = "platform.blue"
//        includeDirs.headerFilterOnly(
//          extractBluez.get().output.get().asFile.resolve("lib").absolutePath,
//          "src/native/include"
//        )
//      }
//    }
//    compilations.all {
//      compileTaskProvider.configure {
//        dependsOn(buildBluez)
//        val staticLib = buildBluez.get().bluezDirection.get().asFile.resolve("lib/.libs/libbluetooth.a")
//        compilerOptions.freeCompilerArgs = listOf(
//          "-include-binary",
//          staticLib.absolutePath
//        )
//      }
//    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":io"))
      api(project(":thread"))
//      api(project(":env"))
//      api("pw.binom:atomic:${pw.binom.Versions.ATOMIC_VERSION}")
//      api(project(":collections"))
//      api(project(":pool"))
//      api("pw.binom:url:${pw.binom.Versions.BINOM_URL_VERSION}")
//      api("pw.binom:uuid:${pw.binom.Versions.BINOM_UUID_VERSION}")
//      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
//    val nativeRunnableMain by creating {
//      dependsOn(commonMain.get())
//    }
//    nativeMain {
//      dependsOn(nativeRunnableMain)
//    }
//    val jvmLikeMain by creating {
//      dependsOn(commonMain.get())
//    }
//    jvmMain {
//      dependsOn(jvmLikeMain)
//    }

    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
//      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
//      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
//      api(project(":charset"))
    }
//    val nativeRunnableMain by creating {
//      dependsOn(commonMain.get())
//    }
//    val posixMain by getting {
//      dependsOn(nativeRunnableMain)
//    }
//    mingwMain {
//      dependsOn(nativeRunnableMain)
//    }
//    val androidNativeMain by getting {
//      dependsOn(nativeRunnableMain)
//    }
    jvmMain.dependencies {
      implementation("net.java.dev.jna:jna:5.16.0")
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }

    jsTest.dependencies {
      api(kotlin("test-js"))
    }
  }
}
tasks {
  val jvmProcessResources by getting
  val dynamicTasks = withType<BuildDynamicTask>()
  dynamicTasks.forEach { buildTask ->
    val copyLibTask = register<Copy>("copyDynamicLib${buildTask.target.get().name.capitalize()}") {
      dependsOn(buildTask)
      from(buildTask.dynamicFile)
      into(layout.buildDirectory.file("processedResources/jvm/main"))
      val sharedLibExtension = when (buildTask.target.get().family) {
        Family.ANDROID,
        Family.LINUX,
          -> "so"

        Family.MINGW -> "dll"
        Family.TVOS,
        Family.WATCHOS,
        Family.IOS,
        Family.OSX,
          -> "dylib"
      }
      rename { "bluetooth_binom_${buildTask.target.get().name}-${project.version}.$sharedLibExtension" }
    }
    jvmProcessResources.dependsOn(copyLibTask)
  }
//  jvmProcessResources.dependsOn(dynamicTasks)
}
tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
