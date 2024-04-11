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
    -"js"
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
