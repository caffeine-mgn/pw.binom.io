plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  /*
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
      android {
          publishAllLibraryVariants()
      }
  }
  jvm()
  linuxX64()
  if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
      linuxArm32Hfp()
  }
  if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
      linuxArm64()
  }
  mingwX64()
  if (pw.binom.Target.MINGW_X86_SUPPORT) {
      mingwX86()
  }
  macosX64()
  js(pw.binom.Target.JS_TARGET) {
      browser {
          testTask {
              useKarma {
                  useFirefoxHeadless()
              }
          }
      }
      nodejs()
  }
   */
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":date"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
