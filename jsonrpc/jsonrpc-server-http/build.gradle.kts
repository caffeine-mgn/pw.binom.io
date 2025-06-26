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
  allTargets {
    config()
    -"wasmJs"
    -"wasmWasi"
    -"js"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.serialization.json)
      api(project(":jsonrpc:jsonrpc-server"))
      api(project(":httpServer"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(libs.kotlinx.coroutines.test)
      api(project(":testing"))
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
