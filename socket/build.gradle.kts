import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import pw.binom.UdpMulticastSenderPlugin
import pw.binom.eachKotlinTest
import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*
import kotlin.time.Duration.Companion.seconds

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.bmuschko.docker-remote-api")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
  id("com.jakewharton.cite")
}
gradle.startParameter.taskRequests.joinToString { it.args.toString() }
fun KotlinNativeTarget.useNativeNet() {
  val headersPath = project.buildFile.parentFile.resolve("src/nativeCommonMain/include")
  val staticBuildTask =
    clangBuildStatic(name = "binom-socket", target = konanTarget) {
      group = "clang"
      konanVersion.set(pw.binom.Versions.KOTLIN_VERSION)
      include(headersPath)
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NEvent.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NSelectedList.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NSelector.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NSocket.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NInetSocketNetworkAddress.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NNetworkAddress.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/NNetworkInterface.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/Network.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/wepoll.c"))
      compileFile(file("${buildFile.parentFile}/src/nativeCommonMain/src/err.c"))
    }
  val targetName = compileTaskName.removePrefix("compileKotlin")
  val directory = targetName[0].lowercase() + targetName.substring(1)
  val removeCacheTask = tasks.create("deleteCache$targetName") {
    doLast {
      val v = layout.buildDirectory.asFile.get().resolve("classes/kotlin/$directory/main/klib/socket.klib")
      if (v.isFile) {
        val rr = v.delete()
        logger.lifecycle("File removed $rr")
      } else {
        logger.lifecycle("File not found!")
      }
    }
  }
  staticBuildTask.dependsOn(removeCacheTask)
  tasks.findByName(compileTaskName)?.dependsOn(staticBuildTask)
  val args = listOf("-include-binary", staticBuildTask.staticFile.asFile.get().absolutePath)
  compilations["main"].kotlinOptions.freeCompilerArgs = args
  compilations["test"].kotlinOptions.freeCompilerArgs = args
  compilations["main"].cinterops {
    create("nativeCommon") {
      defFile = project.file("src/cinterop/native.def")
      packageName = "platform.socket"
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
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(project(":thread"))
      api(project(":testing"))
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
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
//    dependsOn("linuxTest", epollLikeTest)
//    dependsOn("mingwTest", epollLikeTest)
  }
}
apply<UdpMulticastSenderPlugin>()
val udpExtension = extensions.getByType(UdpMulticastSenderPlugin.UdpMulticastSenderExtension::class.java)
tasks {
  val udpEchoServer = pw.binom.plugins.DockerUtils.dockerContanier(
    project = project,
    image = "particle/udp-echo:latest",
    udpPorts = listOf(80 to 8143),
    args = listOf(),
    suffix = "UdpEcho",
    envs = mapOf(
      "IN_PORT" to "80",
    ),
  )
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
  udpExtension.also {
    it.define(
      networkHost = "127.0.0.1",
      ip = "239.255.255.250",
      port = 4321,
      interval = 2.seconds,
      data = "Some Data on 4321",
    )
  }
  val startUdp = withType(UdpMulticastSenderPlugin.UdpMulticastStartSendTasks::class.java)
  val stopUdp = withType(UdpMulticastSenderPlugin.UdpMulticastStopSendTasks::class.java)
  eachKotlinTest {
    it.dependsOn(startUdp)
    it.finalizedBy(stopUdp)
    httpStorage.dependsOn(it)
    udpEchoServer.dependsOn(it)
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
