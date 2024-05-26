import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":core"))
        api(project(":collections"))
      }
    }
    val otherMain by creating {
      dependsOn(commonMain)
    }
    val nativeIconvMain by creating {
      dependsOn(commonMain)
    }

//    val jvmLikeMain by creating {
//      dependsOn(commonMain)
//    }
//    jvmMain {
//      dependsOn(jvmLikeMain)
//    }

    jsMain {
      dependsOn(otherMain)
    }
    linuxMain {
      dependsOn(nativeIconvMain)
    }
    mingwMain {
      dependsOn(nativeIconvMain)
    }
    androidNativeMain {
      dependsOn(otherMain)
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
//        dependsOn("wasm*Main", otherMain)

    val jvmTest by getting {
      dependsOn(commonTest)
      dependencies {
        api(kotlin("test"))
      }
    }

    val jsTest by getting {
      dependsOn(commonTest)
      dependencies {
        api(kotlin("test-js"))
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
