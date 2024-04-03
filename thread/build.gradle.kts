import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useNative() {
  compilations["main"].cinterops {
    create("native") {
      defFile = project.file("src/cinterop/native.def")
      packageName = "platform.common"
    }
  }
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  eachNative {
    useNative()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":collections"))
      api(project(":concurrency"))
      api(project(":metric"))
      api(project(":io"))
    }
    /*
    nativeMain {
      dependsOn(commonMain.get())
    }
    val posixMain by creating {
      dependsOn(nativeMain.get())
    }
    val jvmLikeMain by creating {
      dependsOn(commonMain.get())
    }
    jvmMain {
      dependsOn(jvmLikeMain)
    }
    linuxMain {
      dependsOn(posixMain)
    }
    appleMain {
      dependsOn(posixMain)
    }
    androidNativeMain {
      dependsOn(posixMain)
    }
     */
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
