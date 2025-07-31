import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  androidTarget()
  allTargets {
    config()
    -"js"
    -"wasmWasi"
    -"wasmJs"
  }
  jvm {
    compilations.all {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.JVM_1_8)
      }
    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":thread"))
        api(project(":concurrency"))
        api(libs.kotlinx.coroutines.core)
      }
    }
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(libs.kotlinx.coroutines.test)
      }
    }

    val jvmTest by getting {
      dependencies {
        api(kotlin("test"))
      }
    }
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
