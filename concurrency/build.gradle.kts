

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
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
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
