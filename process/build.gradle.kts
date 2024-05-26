import org.jetbrains.kotlin.konan.target.Family
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useLinux() {
  compilations["main"].cinterops {
    create("linux") {
      defFile = project.file("src/cinterop/linux.def")
      packageName = "platform.common"
    }
  }
}
kotlin {
  allTargets {
    config()
    -"js"
  }
  eachNative {
    if (konanTarget.family != Family.MINGW) {
      useLinux()
    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":core"))
        api(project(":concurrency"))
        api(kotlin("stdlib-common"))
      }
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
      }
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
