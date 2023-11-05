import pw.binom.useDefault

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
      }
    }
    useDefault()
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
