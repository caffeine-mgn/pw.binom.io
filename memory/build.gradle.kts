plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
//    -"jvm"
//    -"js"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib"))
      }
    }

    val commonTest by getting {
      dependencies {
      }
    }
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
