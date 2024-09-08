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
    -"js"
    -"wasmWasi"
    -"wasmJs"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":core"))
        api(project(":network"))
        api(project(":ssl"))
        api(project(":http"))
        api(project(":httpClient"))
        api(project(":compression"))
        api(project(":binom-coroutines"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }

    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(project(":testing"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
