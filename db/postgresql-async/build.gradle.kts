import pw.binom.eachKotlinTest

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.bmuschko.docker-remote-api")
    id("maven-publish")
}

kotlin {
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    macosX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":db"))
                api(project(":date"))
                api(project(":network"))
                api(project(":ssl"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependsOn(commonMain)
            }
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonMain)
            }
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependsOn(commonMain)
            }
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

tasks {
    val postgresServer = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
        image = "postgres:11",
        tcpPorts = listOf(5432 to 6122),
        args = listOf(),
        suffix = "Postgres",
        envs = mapOf(
            "POSTGRES_USER" to "postgres",
            "POSTGRES_PASSWORD" to "postgres",
            "POSTGRES_DB" to "test"
        ),
        healthCheck = "/usr/bin/pg_isready -U postgres"
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
