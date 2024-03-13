import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.kotlin.clang.eachNative

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
}

fun KotlinNativeTarget.useNativeUtils() {
  compilations["main"].cinterops {
    create("native") {
      defFile = project.file("src/cinterop/native_utils.def")
      packageName = "platform.common"
    }
  }
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  eachNative {
    useNativeUtils()
  }
  applyDefaultHierarchyBinomTemplate()
//  applyDefaultHierarchyTemplate()
  sourceSets {
    /*
    val posixMain by creating {
      dependsOn(nativeMain.get())
    }
    linuxMain {
      dependsOn(posixMain)
    }
    androidNativeMain {
      dependsOn(posixMain)
    }
    appleMain {
      dependsOn(posixMain)
    }
     */
    jvmMain.dependencies {
      api("org.jline:jline:3.21.0")
    }
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
