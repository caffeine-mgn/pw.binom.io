import pw.binom.publish.*

plugins {
  kotlin("multiplatform")
  id("kotlinx-serialization")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
//    -"js"
//    -"wasmWasi"
//    -"wasmJs"
//    -"androidNativeArm32"
//    -"androidNativeArm64"
//    -"androidNativeX64"
//    -"androidNativeX86"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":strong"))
      api(libs.kotlinx.serialization.core)
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.serialization.protobuf)
      api(libs.kotlinx.serialization.properties)
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
