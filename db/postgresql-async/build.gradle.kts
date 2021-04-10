import java.util.UUID

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.bmuschko.docker-remote-api")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }
    jvm()
    linuxArm32Hfp {
        binaries {
            staticLib()
        }
    }

    mingwX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    mingwX86 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

//    linuxArm64 {
//        binaries {
//            staticLib {
//            }
//        }
//    }
    macosX64 {
        binaries {
            framework()
        }
    }

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
        val linuxArm32HfpMain by getting {
            dependsOn(commonMain)
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        val mingwX86Main by getting {
            dependsOn(commonMain)
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

val postgresContainerId = UUID.randomUUID().toString()


tasks {
    val pullPostgres = create(
        name = "pullPostgres",
        type = com.bmuschko.gradle.docker.tasks.image.DockerPullImage::class
    ) {
        image.set("postgres:10.15")
    }

    val createPostgres = create(
        name = "createPostgres",
        type = com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer::class
    ) {
        dependsOn(pullPostgres)
        image.set("postgres:10.15")
        imageId.set("postgres:10.15")
        envVars.put("POSTGRES_USER", "postgres")
        envVars.put("POSTGRES_PASSWORD", "postgres")
        envVars.put("POSTGRES_DB", "test")
        hostConfig.portBindings.set(listOf("127.0.0.1:25331:5432"))
        containerId.set(postgresContainerId)
        containerName.set(postgresContainerId)
    }

    val startPostgres = create(
        name = "startPostgres",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStartContainer::class
    ) {
        dependsOn(createPostgres)
        targetContainerId(postgresContainerId)
        doLast {
            Thread.sleep(1000)
        }
    }

    val stopPostgres = create(
        name = "stopPostgres",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStopContainer::class
    ) {
        targetContainerId(postgresContainerId)
    }

    val destoryPostgres = create(
        name = "destoryPostgres",
        type = com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer::class
    ) {
        dependsOn(stopPostgres)
        targetContainerId(postgresContainerId)
    }

    this["jvmTest"].dependsOn(startPostgres)
    this["jvmTest"].finalizedBy(destoryPostgres)

    this["linuxX64Test"].dependsOn(startPostgres)
    this["linuxX64Test"].finalizedBy(destoryPostgres)

    this["mingwX64Test"].dependsOn(startPostgres)
    this["mingwX64Test"].finalizedBy(destoryPostgres)

    this["macosX64Test"].dependsOn(startPostgres)
    this["macosX64Test"].finalizedBy(destoryPostgres)
}
apply<pw.binom.plugins.DocsPlugin>()