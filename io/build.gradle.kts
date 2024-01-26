import pw.binom.useDefault

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
  id("com.jakewharton.cite")
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib"))
        api(project(":metric"))
        api(project(":memory"))
      }
    }

    val commonTest by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
    useDefault()
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
