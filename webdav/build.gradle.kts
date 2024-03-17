import pw.binom.eachKotlinTest

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.bmuschko.docker-remote-api")
  id("maven-publish")
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
      api(project(":httpServer"))
      api(project(":xml"))
      api(project(":date"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(project(":httpClient"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
  }
}

tasks {

  val nats =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "ugeek/webdav:amd64",
      tcpPorts = listOf(80 to 7132),
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
    nats.dependsOn(it)
  }
  /*
      val webdavServerContainerId = UUID.randomUUID().toString()
      val pullWebdavServer = create(
          name = "pullWebdavServer",
          type = com.bmuschko.gradle.docker.tasks.image.DockerPullImage::class
      ) {
          image.set("ugeek/webdav:amd64")
      }

      val createWebdavServer = create(
          name = "createWebdavServer",
          type = com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer::class
      ) {
          dependsOn(pullWebdavServer)
          image.set("ugeek/webdav:amd64")
          imageId.set("ugeek/webdav:amd64")
          envVars.put("USERNAME", "root")
          envVars.put("PASSWORD", "root")
          envVars.put("TZ", "GMT")
          hostConfig.portBindings.set(listOf("127.0.0.1:25371:80"))
          containerId.set(webdavServerContainerId)
          containerName.set(webdavServerContainerId)
      }

      val startWebdavServer = create(
          name = "startWebdavServer",
          type = com.bmuschko.gradle.docker.tasks.container.DockerStartContainer::class
      ) {
          dependsOn(createWebdavServer)
          targetContainerId(webdavServerContainerId)
          doLast {
              Thread.sleep(1000)
          }
      }

      val stopWebdavServer = create(
          name = "stopWebdavServer",
          type = com.bmuschko.gradle.docker.tasks.container.DockerStopContainer::class
      ) {
          targetContainerId(webdavServerContainerId)
      }

      val destroyWebdavServer = create(
          name = "destroyWebdavServer",
          type = com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer::class
      ) {
          dependsOn(stopWebdavServer)
          targetContainerId(webdavServerContainerId)
      }
   */
//    eachKotlinTest {
//        it.dependsOn(startWebdavServer)
//        it.finalizedBy(destroyWebdavServer)
//    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
