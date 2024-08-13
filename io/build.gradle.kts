import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
//  id("com.jakewharton.cite")
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }
  allTargets {
    config()
  }
  js {
    browser {
      testTask {
        useKarma {
          useChromeHeadless()
          useFirefoxHeadless()
        }
      }
    }
    nodejs {
      testTask {
        useKarma()
      }
    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":metric"))
      api(project(":memory"))
    }
    val nonJvmMain by creating
    jsMain {
      dependsOn(nonJvmMain)
    }
    val nativeMain by getting {
      dependsOn(nonJvmMain)
    }
    /*
    val jvmLikeMain by creating {
      dependsOn(commonMain.get())
    }
    jvmMain {
      dependsOn(jvmLikeMain)
    }
     */
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(project(":testing"))
        api(kotlin("test-annotations-common"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
    val jvmTest by getting {
      dependencies {
        api(kotlin("test"))
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
