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
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }
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
                api(project(":core"))
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

    val pullTarantool = create(
        name = "pullTarantool",
        type = com.bmuschko.gradle.docker.tasks.image.DockerPullImage::class
    ) {
        image.set("tarantool/tarantool:2.6.2")
    }

    val createTarantool = create(
        name = "createTarantool",
        type = com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer::class
    ) {
        dependsOn(pullTarantool)
        image.set("tarantool/tarantool:2.6.2")
        imageId.set("tarantool/tarantool:2.6.2")
        envVars.put("TARANTOOL_USER_NAME", "server")
        envVars.put("TARANTOOL_USER_PASSWORD", "server")
        hostConfig.portBindings.set(listOf("127.0.0.1:25321:3301"))
        containerId.set(postgresContainerId)
        containerName.set(postgresContainerId)
    }

    val startTarantool = create(
        name = "startTarantool",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStartContainer::class
    ) {
        dependsOn(createTarantool)
        targetContainerId(postgresContainerId)
        doLast {
            Thread.sleep(1000)
        }
    }

    val stopTarantool = create(
        name = "stopTarantool",
        type = com.bmuschko.gradle.docker.tasks.container.DockerStopContainer::class
    ) {
        targetContainerId(postgresContainerId)
    }

    val destoryTarantool = create(
        name = "destoryTarantool",
        type = com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer::class
    ) {
        dependsOn(stopTarantool)
        targetContainerId(postgresContainerId)
    }

    this["jvmTest"].dependsOn(startTarantool)
    this["jvmTest"].finalizedBy(destoryTarantool)

    this["linuxX64Test"].dependsOn(startTarantool)
    this["linuxX64Test"].finalizedBy(destoryTarantool)

    this["mingwX64Test"].dependsOn(startTarantool)
    this["mingwX64Test"].finalizedBy(destoryTarantool)

    this["macosX64Test"].dependsOn(startTarantool)
    this["macosX64Test"].finalizedBy(destoryTarantool)
}
apply<pw.binom.plugins.DocsPlugin>()