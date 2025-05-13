import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
  androidTarget{
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  jvm {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
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
      api(libs.kotlinx.coroutines.core)
    }
    val nonJvmMain by getting
//    val commonWasmMain by getting {
//      dependsOn(nativeMain.get())
//    }
//    val nativeMain by getting {
//      dependsOn(nonJvmMain)
//    }
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
        api(libs.kotlinx.coroutines.test)
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
