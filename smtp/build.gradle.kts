plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  applyDefaultHierarchyTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":network"))
      api(project(":ssl"))
      api(project(":http"))
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
