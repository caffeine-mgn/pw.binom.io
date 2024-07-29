import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.allTargets
import pw.binom.publish.applyDefaultHierarchyBinomTemplate

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
  id("com.jakewharton.cite")
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
  allTargets{
    config()
  }
  eachNative {
    useNative()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":io"))
      api(project(":env"))
      api("pw.binom:atomic:${pw.binom.Versions.ATOMIC_VERSION}")
      api(project(":collections"))
      api(project(":pool"))
      api("pw.binom:url:${pw.binom.Versions.BINOM_URL_VERSION}")
      api("pw.binom:uuid:${pw.binom.Versions.BINOM_UUID_VERSION}")
    }
//    val nativeRunnableMain by creating {
//      dependsOn(commonMain.get())
//    }
//    nativeMain {
//      dependsOn(nativeRunnableMain)
//    }
//    val jvmLikeMain by creating {
//      dependsOn(commonMain.get())
//    }
//    jvmMain {
//      dependsOn(jvmLikeMain)
//    }

    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api(project(":charset"))
    }
    val nativeRunnableMain by creating {
      dependsOn(commonMain.get())
    }
    val posixMain by getting {
      dependsOn(nativeRunnableMain)
    }
    mingwMain {
      dependsOn(nativeRunnableMain)
    }
    val androidNativeMain by getting {
      dependsOn(nativeRunnableMain)
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }

    jsTest.dependencies {
      api(kotlin("test-js"))
    }
  }
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
