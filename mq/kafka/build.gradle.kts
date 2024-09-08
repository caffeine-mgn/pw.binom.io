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
    -"wasmWasi"
    -"wasmJs"
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
      api(project(":coroutines"))
      api(kotlin("test-annotations-common"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
  }
}

//tasks {
//  val nats =
//    DockerUtils.dockerContanier(
//      project = project,
//      image = "nats:2.10.11",
//      tcpPorts = listOf(4222 to 8122),
//      args = listOf("-js"),
//      suffix = "Nats",
//    )
//
//  eachKotlinTest {
//    nats.dependsOn(it)
//  }
//}

apply<pw.binom.plugins.ConfigPublishPlugin>()
