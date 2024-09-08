import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  id("com.jakewharton.cite")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
    wasmJsMain.dependencies {
      api(kotlin("test-wasm-js"))
    }
    wasmWasiMain.dependencies {
      api(kotlin("test-wasm-wasi"))
    }
    jsMain.dependencies {
      api(kotlin("test-js"))
    }
    jvmMain.dependencies {
      api(kotlin("test"))
      api(kotlin("test-junit"))
    }
    jvmTest.dependencies {

    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
