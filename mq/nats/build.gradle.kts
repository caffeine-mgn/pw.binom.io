import pw.binom.eachKotlinTest
import pw.binom.plugins.DockerUtils
import pw.binom.publish.*

plugins {
  kotlin("multiplatform")
  id("com.bmuschko.docker-remote-api")
  id("maven-publish")
  id("kotlinx-serialization")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
    -"js"
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":mq"))
      api(project(":date"))
      api(project(":network"))
      api(project(":socket"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      api("org.jetbrains.kotlinx:kotlinx-serialization-json:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
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
  val nats =
    DockerUtils.dockerContanier(
      project = project,
      image = "nats:2.10.11",
      tcpPorts = listOf(4222 to 8122),
      args = listOf("-js"),
      suffix = "Nats",
    )

  eachKotlinTest {
    nats.dependsOn(it)
  }
}

tasks {
  withType(Test::class) {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging.showCauses = true
    testLogging.showExceptions = true
    testLogging.showStackTraces
  }
}
/*
val natsContainerId1 = UUID.randomUUID().toString()
val natsContainerId2 = UUID.randomUUID().toString()
val nats1ConnectPort = 4222
val nats1ClasterPort = 6222

val nats2ConnectPort = 4223
val nats2ClasterPort = 6223


tasks {

    val pullNats = create(
        name = "pullNats",
        type = com.bmuschko.gradle.docker.tasks.image.DockerPullImage::class
    ) {
        image.set("nats:2.1.9")
    }

    val createNats1 = create(
        name = "createNats1",
        type = com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer::class
    ) {
        dependsOn(pullNats)
        image.set("nats:2.1.9")
        imageId.set("nats:2.1.9")
        cmd.add("--cluster")
        cmd.add("nats://0.0.0.0:6222")
        cmd.add("--routes=nats://host.docker.internal:$nats2ClasterPort")
        cmd.add("-DVV")
        hostConfig.portBindings.set(
            listOf(
                "127.0.0.1:$nats1ConnectPort:4222",
                "127.0.0.1:$nats1ClasterPort:6222",
                "127.0.0.1:8222:8222"
            )
        )
        containerId.set(natsContainerId1)
        containerName.set(natsContainerId1)
    }

    val startNats1 = create(
        name = "startNats1",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStartContainer::class
    ) {
        dependsOn(createNats1)
        targetContainerId(natsContainerId1)
        doLast {
            Thread.sleep(1000)
        }
    }

    val stopNats1 = create(
        name = "stopNats1",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStopContainer::class
    ) {
        targetContainerId(natsContainerId1)
    }

    val destoryNats1 = create(
        name = "destoryNats1",
        type = com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer::class
    ) {
        dependsOn(stopNats1)
        targetContainerId(natsContainerId1)
    }


    //-----------------
    val createNats2 = create(
        name = "createNats2",
        type = com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer::class
    ) {
        dependsOn(pullNats)
        image.set("nats:2.1.9")
        imageId.set("nats:2.1.9")
        cmd.add("--cluster")
        cmd.add("nats://0.0.0.0:6222")
        cmd.add("--routes=nats://host.docker.internal:$nats1ClasterPort")
        cmd.add("-DVV")
        hostConfig.portBindings.set(
            listOf(
                "127.0.0.1:$nats2ConnectPort:4222",
                "127.0.0.1:$nats2ClasterPort:6222",
                "127.0.0.1:8223:8222"
            )
        )
        containerId.set(natsContainerId2)
        containerName.set(natsContainerId2)
    }

    val startNats2 = create(
        name = "startNats2",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStartContainer::class
    ) {
        dependsOn(createNats2)
        targetContainerId(natsContainerId2)
        doLast {
            Thread.sleep(1000)
        }
    }

    val stopNats2 = create(
        name = "stopNats2",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStopContainer::class
    ) {
        targetContainerId(natsContainerId2)
    }

    val destoryNats2 = create(
        name = "destoryNats2",
        type = com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer::class
    ) {
        dependsOn(stopNats2)
        targetContainerId(natsContainerId2)
    }
    //-----------------

    this["jvmTest"].dependsOn(startNats1)
    this["jvmTest"].finalizedBy(destoryNats1)

    this["linuxX64Test"].dependsOn(startNats1)
    this["linuxX64Test"].finalizedBy(destoryNats1)

    this["mingwX64Test"].dependsOn(startNats1)
    this["mingwX64Test"].finalizedBy(destoryNats1)

    this["macosX64Test"].dependsOn(startNats1)
    this["macosX64Test"].finalizedBy(destoryNats1)

    //------------

    this["jvmTest"].dependsOn(startNats2)
    this["jvmTest"].finalizedBy(destoryNats2)

    this["linuxX64Test"].dependsOn(startNats2)
    this["linuxX64Test"].finalizedBy(destoryNats2)

    this["mingwX64Test"].dependsOn(startNats2)
    this["mingwX64Test"].finalizedBy(destoryNats2)

    this["macosX64Test"].dependsOn(startNats2)
    this["macosX64Test"].finalizedBy(destoryNats2)
}
*/
apply<pw.binom.plugins.ConfigPublishPlugin>()
