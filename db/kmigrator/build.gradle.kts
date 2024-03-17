plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jmailen.kotlinter")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":db"))
        api(project(":logger"))
      }
    }
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
      }
    }
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
