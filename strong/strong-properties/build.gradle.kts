import pw.binom.publish.*

plugins {
  kotlin("multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":strong"))
      api(project(":validate"))
      api(project(":properties-serialization"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
