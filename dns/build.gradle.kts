plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  applyDefaultHierarchyTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":network"))
      api("pw.binom:bitarray:0.2.2")
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
