import pw.binom.publish.allTargets

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.bmuschko.docker-remote-api")
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
  applyDefaultHierarchyTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":network"))
      api(project(":date"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api(project(":testing"))
    }
    jvmTest.dependencies {
      api(kotlin("test"))
    }
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
