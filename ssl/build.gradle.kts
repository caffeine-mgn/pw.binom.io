import pw.binom.OpenSSLBuildTask
import pw.binom.OpenSSLDownloadTask
import pw.binom.OpenSSLUnpackTask
import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.dependsOn

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  applyDefaultHierarchyTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":core"))
        api(project(":network"))
        api(project(":file"))
        api(project(":date"))
        api(project(":socket"))
        api(project(":concurrency"))
        api("com.ionspin.kotlin:bignum:${pw.binom.Versions.IONSPIN_BIGNUM_VERSION}")
      }
    }
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
      }
    }
    val jvmMain by getting {
      dependencies {
        api("org.bouncycastle:bcprov-jdk15on:1.68")
        api("org.bouncycastle:bcpkix-jdk15on:1.68")
      }
    }
//    useDefault()
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()

tasks {

  val downloadSsl by creating(OpenSSLDownloadTask::class) {
  }
  val extractSsl by creating(OpenSSLUnpackTask::class) {
    dependsOn(downloadSsl)
    this.input.set(downloadSsl.output)
  }

  var lastBuildTask: OpenSSLBuildTask? = null

  kotlin.eachNative {
    val headersPath = file("${buildFile.parent}/src/cinterop/include")
    val keccakStaticTask =
      clangBuildStatic(name = "keccak", target = this.konanTarget) {
        konanVersion.set(pw.binom.Versions.KOTLIN_VERSION)
        include(headersPath.resolve("keccak"))
        compileArgs.addAll(listOf("-std=c99", "-O3", "-g"))
        compileFile(
          file("${buildFile.parentFile}/src/cinterop/include/keccak/sha3.c"),
        )
      }
    val buildOpensslTask = register("buildOpenSSL$targetName", OpenSSLBuildTask::class.java)
    findByName(compileTaskName)?.let {
      it.dependsOn(keccakStaticTask)
      it.dependsOn(buildOpensslTask)
    }
    buildOpensslTask.configure {

      if (lastBuildTask != null) {
        mustRunAfter(lastBuildTask)
      }
      lastBuildTask = this
      tempDirForObjectFiles.set(target.map { t -> RegularFile { project.buildDir.resolve("openssl/${t.name}/static") } })
      dependsOn(extractSsl)
      opensslDirection.set(extractSsl.output)
//      afterConfig()
      target.set(konanTarget)
    }

    compilations["main"].cinterops {
      val openssl by creating {
        defFile = project.file("src/cinterop/openssl.def")
        packageName = "platform.openssl"
        includeDirs.headerFilterOnly(headersPath.absolutePath)
      }
    }
    val libFile = buildOpensslTask.get().staticLib.get().asFile
    val args =
      listOf(
        "-include-binary",
        libFile.absolutePath,
        "-include-binary",
        keccakStaticTask.staticFile.asFile.get().absolutePath,
        "-opt-in=kotlin.RequiresOptIn",
      )
    compilations["main"].kotlinOptions.freeCompilerArgs = args
    compilations["test"].kotlinOptions.freeCompilerArgs = args
  }
}
