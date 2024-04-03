import org.jetbrains.kotlin.konan.target.Family
import pw.binom.kotlin.clang.eachNative
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
  eachNative {
    if (konanTarget.family == Family.ANDROID || konanTarget.family == Family.LINUX) {
      compilations["main"].cinterops {
        create("native") {
          defFile = project.file("src/cinterop/linux.def")
          packageName = "platform.common"
        }
      }
    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":io"))
      api(project(":file"))
      api(project(":env"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(project(":concurrency"))
    }
    jvmTest {
      dependsOn(commonTest.get())
      dependencies {
        api(kotlin("test-junit"))
      }
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
