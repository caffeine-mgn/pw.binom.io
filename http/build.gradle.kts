import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
//  id("com.jakewharton.cite")
}
apply<pw.binom.KotlinConfigPlugin>()

fun KotlinNativeTarget.useNativeUtils() {
  compilations["main"].cinterops {
    create("native") {
      definitionFile.set(project.file("src/nativeMain/interop/encode.def"))
      packageName = "platform.websocket"
    }
  }
}

kotlin {
  allTargets{
    config()
  }
  applyDefaultHierarchyBinomTemplate()
  eachNative {
    useNativeUtils()
  }
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":charset"))
      api(project(":coroutines"))
      api(project(":network-common"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api(project(":httpServer"))
      api(project(":testing"))
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
    val manualMain by creating {
      dependsOn(commonMain.get())
    }

    dependsOn("jsMain", manualMain)
    dependsOn("jvmLikeMain", manualMain)
    dependsOn("wasm*Main", manualMain)
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
