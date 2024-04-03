import pw.binom.eachKotlinTest
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.bmuschko.docker-remote-api")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":http"))
      }
    }
    val runnableMain by getting {
      dependencies {
        api(project(":ssl"))
        api(project(":compression"))
      }
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(project(":httpServer"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()

tasks {
  val httpWsEcho =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "jmalloc/echo-server",
      tcpPorts = listOf(8080 to 7142),
      args = listOf(),
      suffix = "WS-EchoServer",
    )

  val httpStorage =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "ugeek/webdav:amd64",
      tcpPorts = listOf(80 to 7143),
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
    httpWsEcho.dependsOn(it)
    httpStorage.dependsOn(it)
  }
}
