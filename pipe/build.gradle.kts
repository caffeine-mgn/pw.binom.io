import org.jetbrains.kotlin.konan.target.Family
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  id("com.jakewharton.cite")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}

apply<pw.binom.KotlinConfigPlugin>()

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useLinux() {
  compilations["main"].cinterops {
    create("linux") {
      definitionFile.set(project.file("src/cinterop/linux.def"))
      packageName = "platform.common"
    }
  }
}

kotlin {
  allTargets {
    config()
    -"js"
    -"wasmWasi"
    -"wasmJs"
  }

  eachNative {
    if (konanTarget.family != Family.MINGW) {
      useLinux()
    }
  }

  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":io"))
    }
    commonTest.dependencies {
      api(project(":thread"))
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
    jvmTest.dependencies {
      api(kotlin("test"))
    }
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
