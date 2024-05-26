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
  /*
  jvm()
  linuxX64()
  js(pw.binom.Target.JS_TARGET) {
      browser()
      nodejs()
  }
  linuxArm32Hfp()
  mingwX64()
  if (pw.binom.Target.MINGW_X86_SUPPORT) {
      mingwX86()
  }
  linuxArm64()
  macosX64()
   */
  allTargets{
    config()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":collections"))
    }

    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }

    /*
    val jvmTest by getting {
        dependsOn(commonTest)
        dependencies {
            api(kotlin("test-junit"))
        }
    }
    val linuxX64Test by getting {
        dependsOn(commonTest)
    }
     */
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
