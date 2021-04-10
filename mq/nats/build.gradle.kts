import java.util.UUID
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.bmuschko.docker-remote-api")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()

    linuxX64 { // Use your target instead.
        binaries {
            staticLib()
        }
    }

    linuxArm32Hfp {
        binaries {
            staticLib()
        }
    }

//    linuxArm64 {
//        binaries {
//            staticLib()
//        }
//    }

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

    macosX64 {
        binaries {
            framework()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":network"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
//        val linuxArm64Main by getting {
//            dependsOn(commonMain)
//        }
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
            hostConfig.portBindings.set(listOf("127.0.0.1:$nats1ConnectPort:4222", "127.0.0.1:$nats1ClasterPort:6222", "127.0.0.1:8222:8222"))
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
            hostConfig.portBindings.set(listOf("127.0.0.1:$nats2ConnectPort:4222", "127.0.0.1:$nats2ClasterPort:6222", "127.0.0.1:8223:8222"))
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
apply<pw.binom.plugins.DocsPlugin>()