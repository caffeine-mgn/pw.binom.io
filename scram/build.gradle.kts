import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("kotlinx-serialization")
  id("com.bmuschko.docker-remote-api")
  id("maven-publish")
}

apply {
  plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
  allTargets {
    -"js"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":ssl"))
    }
  }
}

apply<pw.binom.plugins.DocsPlugin>()
