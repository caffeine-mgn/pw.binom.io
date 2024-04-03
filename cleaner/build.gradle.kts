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
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
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

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
