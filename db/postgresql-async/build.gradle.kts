import pw.binom.eachKotlinTest
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.bmuschko.docker-remote-api")
  id("maven-publish")
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
      api(kotlin("stdlib-common"))
      api(project(":db"))
      api(project(":date"))
      api(project(":network"))
      api(project(":ssl"))
      api(project(":scram"))
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
  val postgresServer =
    pw.binom.plugins.DockerUtils.dockerContanier(
      project = project,
      image = "postgres:14",
      tcpPorts = listOf(5432 to 6122),
      args = listOf(),
      suffix = "Postgres",
      envs =
        mapOf(
          "POSTGRES_USER" to "postgres",
          "POSTGRES_PASSWORD" to "postgres",
          "POSTGRES_DB" to "test",
        ),
      healthCheck = "/usr/bin/pg_isready -U postgres",
      withHealthCheck = true,
    )
  postgresServer.create.configure {
//        this.cmd.add(
//            """until /usr/bin/pg_isready || [ ${'$'}RETRIES -eq 0 ]; do
//            sleep 1
//            done
//            """
//        )
    this.attachStdin.set(true)
    this.attachStdout.set(true)
    this.attachStderr.set(true)
  }
//    val waitUntilStart =
//        this.register("waitPostgres", com.bmuschko.gradle.docker.tasks.container.DockerExecContainer::class.java) {
//            dependsOn(postgresServer.start)
//            containerId.set(postgresServer.containerId)
//            withCommand("/bin/bash -c \"echo 1\"")
// //        this.withCommand(
// //            """/bin/bash -c "until /usr/bin/pg_isready || [ ${'$'}RETRIES -eq 0 ]; do
// //            sleep 1
// //            done"
// //            """
// //        )
//        }
  eachKotlinTest {
    postgresServer.dependsOn(it)
//        it.dependsOn(waitUntilStart)
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
