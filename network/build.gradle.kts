import pw.binom.eachKotlinTest
import pw.binom.useDefault

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
  id("com.bmuschko.docker-remote-api")
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
  id("com.jakewharton.cite")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":core"))
      }
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(project(":date"))
        api(project(":charset"))
        api(project(":coroutines"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
    useDefault()

    val runnableMain by getting {
      dependencies {
        api(project(":env"))
        api(project(":concurrency"))
        api(project(":thread"))
        api(project(":collections"))
        api(project(":socket"))
        api(kotlin("stdlib-common"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
  }
}
tasks {
  withType(Test::class) {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging.showCauses = true
    testLogging.showExceptions = true
    testLogging.showStackTraces = true
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
  }

  val httpStorage =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "ugeek/webdav:amd64",
      tcpPorts = listOf(80 to 7141),
      args = listOf(),
      suffix = "WebDav",
      envs =
        mapOf(
          "USERNAME" to "root",
          "PASSWORD" to "root",
          "TZ" to "GMT",
        ),
    )

  eachKotlinTest {
    httpStorage.dependsOn(it)
  }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
