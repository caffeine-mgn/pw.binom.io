import pw.binom.useDefault

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("kotlinx-serialization")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":http"))
        api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
        api("pw.binom:bitarray:${pw.binom.Versions.BITARRAY_VERSION}")
      }
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
    useDefault()
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()