import org.jetbrains.kotlin.konan.target.Family
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.jakewharton.cite")
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

    commonMain.dependencies {
      api(project(":core"))
      api(project(":concurrency"))
      api(kotlin("stdlib-common"))
      api(project(":pipe"))
    }
    commonTest.dependencies {
      api(project(":thread"))
      api(kotlin("test"))
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
    nativeMain.dependencies {

    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
