import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import pw.binom.eachKotlinTest
import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.dependsOn

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.bmuschko.docker-remote-api")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
  id("com.jakewharton.cite")
}

fun KotlinNativeTarget.useNativeNet() {
  val headersPath = project.buildFile.parentFile.resolve("src/nativeCommonMain/include")
  val staticBuildTask =
    clangBuildStatic(name = "binom-socket", target = konanTarget) {
      group = "clang"
      konanVersion.set(pw.binom.Versions.KOTLIN_VERSION)
      include(headersPath)
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/Event.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/SelectedList.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/Selector.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/Socket.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NativeNetworkAddress.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NetworkInterface.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/wepoll.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/err.c"))
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
  allTargets {
    -"js"
  }

  eachNative {
    if (konanTarget.family.isAppleFamily) {
      useNativeMacos()
    }
    if (konanTarget.family == Family.MINGW) {
      useNativeMingw()
    }
    useNativeUtils()
    useNativeNet()
  }
  applyDefaultHierarchyTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":io"))
      api(project(":concurrency"))
    }
    commonTest.dependencies {
      api(project(":thread"))
    }
    val jvmLikeMain by creating {
      dependsOn(commonMain.get())
    }
    jvmMain {
      dependsOn(jvmLikeMain)
    }
    val nativeRunnableMain by creating {
      dependsOn(commonMain.get())
    }
    val posixMain by creating {
      dependsOn(nativeRunnableMain)
    }

    val nativeRunnableTest by creating {
      dependsOn(commonTest.get())
    }
    val epollLikeMain by creating {
      dependsOn(nativeRunnableMain)
    }
    val epollLikeTest by creating {
      dependsOn(nativeRunnableTest)
    }
    val linuxMain by getting
    linuxMain {
      dependsOn(epollLikeMain)
      dependsOn(posixMain)
    }
    androidNativeMain {
      dependsOn(linuxMain)
      dependsOn(posixMain)
    }
    appleMain {
      dependsOn(posixMain)
    }
    mingwMain {
      dependsOn(epollLikeMain)
    }
//    dependsOn("linuxTest", epollLikeTest)
//    dependsOn("mingwTest", epollLikeTest)
  }
}

tasks {
  val httpStorage =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "ugeek/webdav:amd64",
      tcpPorts = listOf(80 to 7143),
      args = listOf(),
      suffix = "WebDav",
      envs =
        mapOf(
          "USERNAME" to "root",
          "PASSWORD" to "root",
          "TZ" to "GMT",
        ),
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
apply<pw.binom.plugins.ConfigPublishPlugin>()
