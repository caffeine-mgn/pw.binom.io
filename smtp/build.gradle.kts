import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":network"))
      api(project(":ssl"))
      api(project(":http"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api(project(":testing"))
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
