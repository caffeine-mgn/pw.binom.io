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
//  allTargets()
  jvm()
  js(IR){
    browser()
    nodejs()
  }
//  applyDefaultHierarchyTemplate()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting
    val jsMain by getting {
      dependsOn(commonMain)
    }
//    commonMain.dependencies {
//      api(kotlin("stdlib-common"))
//      api(kotlin("stdlib"))
//    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
    /*
    jsMain {
      dependsOn(commonMain)
      dependencies {
//        api(kotlin("stdlib-js"))
      }
    }
    */
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
