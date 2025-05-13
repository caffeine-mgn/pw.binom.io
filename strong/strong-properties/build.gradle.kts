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
  allTargets{
    config()
  }
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
      api(libs.kotlinx.coroutines.core)
      api(libs.kotlinx.coroutines.test)
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
