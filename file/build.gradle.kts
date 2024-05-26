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

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useNative() {
  compilations["main"].cinterops {
    create("native") {
      defFile = project.file("src/cinterop/native.def")
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
    useNative()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":charset"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(project(":concurrency"))
    }
    jvmTest {
      dependsOn(commonTest.get())
      dependencies {
        api(kotlin("test-junit"))
      }
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
