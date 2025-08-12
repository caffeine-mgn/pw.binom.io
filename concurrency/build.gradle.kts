import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
    -"jvm"
    -"js"
    -"wasmWasi"
    -"wasmJs"
  }
  jvm {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }
  applyDefaultHierarchyBinomTemplate()
//  applyDefaultHierarchyTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":collections"))
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
    val posixMain by getting
    androidNativeMain {
      dependsOn(posixMain)
    }
    dependsOn("linux*Main", posixMain)
//    dependsOn("ios*Main", posixMain)
//    dependsOn("mac*Main", posixMain)
    appleMain{
      dependsOn(posixMain)
    }
//    val jvmLikeMain by creating {
//      dependsOn(commonMain.get())
//    }
//    jvmMain{
//      dependsOn(jvmLikeMain)
//    }
//    androidMain {
//      dependsOn(jvmLikeMain)
//    }
//    val posixMain by creating{
//      dependsOn(commonMain)
//    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
