import pw.binom.useDefault

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("kotlinx-serialization")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":http"))
        api(project(":httpClient"))
        api(libs.kotlinx.serialization.core)
      }
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(libs.kotlinx.coroutines.test)
      }
    }
    useDefault()
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
