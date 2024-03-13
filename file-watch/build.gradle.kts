import org.jetbrains.kotlin.konan.target.Family
import pw.binom.kotlin.clang.eachNative

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
  applyDefaultHierarchyTemplate()
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
    val jvmLikeMain by creating {
      dependsOn(commonMain.get())
    }
    jvmMain {
      dependsOn(jvmLikeMain)
    }
    val linuxLikeMain by creating {
      dependsOn(commonMain.get())
    }
    appleMain {
      dependsOn(linuxLikeMain)
    }
    linuxMain {
      dependsOn(linuxLikeMain)
    }
    androidNativeMain {
      dependsOn(linuxLikeMain)
    }
    val jvmTest by getting {
      dependsOn(commonTest.get())
      dependencies {
        api(kotlin("test-junit"))
      }
    }
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
