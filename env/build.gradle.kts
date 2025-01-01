import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
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

fun KotlinNativeTarget.useMacUtils() {
  compilations["main"].cinterops {
    create("macNative") {
      definitionFile.set(project.file("src/cinterop/mac.def"))
      packageName = "platform.env.mac"
    }
  }
}

fun KotlinNativeTarget.usePosixUtils() {
  compilations["main"].cinterops {
    create("native") {
      definitionFile.set(project.file("src/cinterop/common.def"))
      packageName = "platform.env.common"
    }
  }
}

kotlin {
  allTargets{
    config()
  }
  eachNative {
    if (this.konanTarget.family != Family.MINGW){
      usePosixUtils()
    }
    if (this.konanTarget.family.isAppleFamily){
      useMacUtils()
    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":collections"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
  }
}

tasks {
//    val generateVersion = create("generateVersion") {
//        val sourceDir = project.buildDir.resolve("gen/pw/binom")
//        sourceDir.mkdirs()
//        val versionSource = sourceDir.resolve("version.kt")
//        outputs.files(versionSource)
//        inputs.property("version", project.version)
//
//        versionSource.writeText(
//            """package pw.binom
//
// const val BINOM_VERSION = "${project.version}"
// """,
//        )
//    }
//    eachKotlinCompile {
//        it.dependsOn(generateVersion)
//    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
