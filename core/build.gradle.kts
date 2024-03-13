import pw.binom.kotlin.clang.eachNative
import java.util.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.github.ManifestClasspath") version "0.1.0-RELEASE"
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
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
  allTargets()
  eachNative {
    useNative()
  }
  applyDefaultHierarchyTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":io"))
      api(project(":env"))
      api("pw.binom:atomic:${pw.binom.Versions.ATOMIC_VERSION}")
      api(project(":collections"))
      api(project(":pool"))
      api(project(":url"))
      api("pw.binom:uuid:${pw.binom.Versions.BINOM_UUID_VERSION}")
    }
    val nativeRunnableMain by creating {
      dependsOn(commonMain.get())
    }
    nativeMain {
      dependsOn(nativeRunnableMain)
    }
    val jvmLikeMain by creating {
      dependsOn(commonMain.get())
    }
    jvmMain {
      dependsOn(jvmLikeMain)
    }

    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api(project(":charset"))
    }
    jvmTest.dependencies {
      api(kotlin("test"))
    }

    jsTest.dependencies {
      api(kotlin("test-js"))
    }
  }
}

fun makeTimeFile() {
  val dateDir = file("$buildDir/tmp-date")
  dateDir.mkdirs()
  val tzFile = file("$dateDir/currentTZ")
  tzFile.delete()
  tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
}

tasks {
  withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
    it.doFirst {
      makeTimeFile()
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
