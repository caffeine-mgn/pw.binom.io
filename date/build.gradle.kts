import pw.binom.eachKotlinCompile
import java.util.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val implMain by creating {
      dependsOn(commonMain.get())
    }
    jsMain {
      dependsOn(implMain)
    }
    val posixMain by getting {
      dependsOn(implMain)
    }
    mingwMain {
      dependsOn(implMain)
    }

    commonTest {
      kotlin.srcDir("build/gen")
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
//        api(project(":env"))
      }
    }

    jsTest.dependencies {
      api(kotlin("test-js"))
    }
  }
}

tasks {

  fun generateDate() {
    val sourceDir = project.layout.buildDirectory.map { it.dir("gen/pw/binom/date") }
      .get()
      .asFile
    sourceDir.mkdirs()
    val versionSource = sourceDir.resolve("test_data.kt")
    versionSource.writeText(
      """package pw.binom.date

val test_data_currentTZ get() = ${TimeZone.getDefault().rawOffset.let { it / 1000 / 60 }}
val test_data_now get() = ${Date().time}
""",
    )
  }

  eachKotlinCompile {
    it.doFirst {
      generateDate()
    }
  }

  withType(Test::class) {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging.showCauses = true
    testLogging.showExceptions = true
    testLogging.showStackTraces = true
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
