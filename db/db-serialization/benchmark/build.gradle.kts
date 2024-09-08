plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("kotlinx-serialization")
  id("com.bmuschko.docker-remote-api")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
  id("org.jetbrains.kotlin.plugin.allopen") version "1.9.20"
}
apply<pw.binom.KotlinConfigPlugin>()

allOpen {
  annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
  allTargets {
    -"js"
    -"wasmWasi"
    -"wasmJs"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":db"))
        api(project(":collections"))
        api(project(":db:db-serialization"))
        api(project(":db:sqlite"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.9")
      }
    }
  }
}
