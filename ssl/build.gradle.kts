import pw.binom.DownloadTask
import pw.binom.OpenSSLBuildTask
import pw.binom.ExtractTask
import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
    -"js"
    -"wasmJs"
    -"wasmWasi"
  }
  linuxX64 {
    this.compilerOptions.verbose.set(true)
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":core"))
      api(project(":file"))
      api(project(":date"))
      api(project(":concurrency"))
      api("com.ionspin.kotlin:bignum:${pw.binom.Versions.IONSPIN_BIGNUM_VERSION}")
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
    jvmMain.dependencies {
      api("org.bouncycastle:bcprov-jdk15on:1.68")
      api("org.bouncycastle:bcpkix-jdk15on:1.68")
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()

tasks {

  val downloadSsl by creating(DownloadTask::class) {
    output.set(project.layout.buildDirectory.file("openssl/openssl.zip"))
    url.set("https://github.com/openssl/openssl/archive/refs/tags/openssl-3.1.1.zip")
  }
  val extractSsl by creating(ExtractTask::class) {
    dependsOn(downloadSsl)
    output.set(project.layout.buildDirectory.file("openssl/source"))
    input.set(downloadSsl.output)
  }

//  var lastBuildTask: OpenSSLBuildTask? = null

  kotlin.eachNative {
    val headersPath = file("${buildFile.parent}/src/cinterop/include")
    val keccakStaticTask =
      clangBuildStatic(name = "keccak", target = this.konanTarget) {
        group = "clang"
        konanVersion.set(pw.binom.Versions.KOTLIN_VERSION)
        include(headersPath.resolve("keccak"))
        compileArgs.addAll(listOf("-std=c99", "-O3", "-g"))
        compileFile(
          file("${buildFile.parentFile}/src/cinterop/include/keccak/sha3.c"),
        )
      }
    val buildOpensslTask = register("buildOpenSSL$targetName", OpenSSLBuildTask::class.java)
//    findByName(compileTaskName)?.let {
//
//    }
    buildOpensslTask.configure {
      dependsOn(extractSsl)
      opensslDirection.set(extractSsl.output)
      target.set(konanTarget)
    }

    compilations["main"].cinterops {
      val openssl by creating {
        definitionFile.set(project.file("src/cinterop/openssl.def"))
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
    compilations.all {
      compileTaskProvider.configure {
        dependsOn(keccakStaticTask)
        dependsOn(buildOpensslTask)
        compilerOptions.freeCompilerArgs = args
      }
    }
  }
}
