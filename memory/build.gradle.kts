import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}

fun KotlinNativeTarget.useNativeUtils() {
  compilations["main"].cinterops {
    create("native") {
      definitionFile.set(project.file("src/cinterop/native.def"))
      packageName = "platform.memory.common"
    }
  }
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
//    -"jvm"
//    -"js"
  }

  eachNative {
    useNativeUtils()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib"))
    }
    val runnableNativeMain by creating {
      dependsOn(commonMain.get())
    }
    val anyWasmMain by creating {
      dependsOn(commonMain.get())
    }
    val posixMain by getting {
      dependsOn(runnableNativeMain)
    }
    androidNativeMain {
      dependsOn(runnableNativeMain)
    }
    mingwMain {
      dependsOn(runnableNativeMain)
    }
    wasmJsMain {
      dependsOn(anyWasmMain)
    }
    wasmWasiMain{
      dependsOn(anyWasmMain)
    }
    dependsOn("*Native*Main", runnableNativeMain)

    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
