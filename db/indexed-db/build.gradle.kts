import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("kotlinx-serialization")
  id("com.bmuschko.docker-remote-api")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()

kotlin {
  js(IR) {
    browser()
    nodejs()
  }

  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":db"))
        api(project(":collections"))
        api(project(":db:db-serialization-annotations"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(project(":db:sqlite"))
        api(project(":network"))
        api(project(":db:postgresql-async"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
  }
}

// if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//  apply<pw.binom.plugins.AndroidSupportPlugin>()
// }
apply<pw.binom.plugins.ConfigPublishPlugin>()
