import pw.binom.eachKotlinTest
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.bmuschko.docker-remote-api")
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
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":db"))
      api(project(":date"))
      api(project(":network"))
      api(project(":ssl"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}

tasks {
  val tarantool =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "tarantool/tarantool:2.6.2",
      tcpPorts = listOf(3301 to 7040),
      args = listOf(),
      suffix = "TarantoolServer",
      envs =
        mapOf(
          "TARANTOOL_USER_NAME" to "server",
          "TARANTOOL_USER_PASSWORD" to "server",
        ),
    )

  eachKotlinTest {
    tarantool.dependsOn(it)
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
