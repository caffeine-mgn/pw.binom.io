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
  allTargets {
    config()
    -"js"
    -"wasmWasi"
    -"wasmJs"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":crc"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(project(":file"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
    jvmTest {
      dependsOn(commonTest.get())
      dependencies {
        api(kotlin("test"))
      }
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
