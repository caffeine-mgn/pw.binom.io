import pw.binom.eachKotlinTest
import pw.binom.plugins.DockerUtils

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.bmuschko.docker-remote-api")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

kotlin {
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    macosX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":network"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
            val linuxArm64Main by getting {
                dependsOn(commonMain)
            }
        }
        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
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

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {}
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmMain)
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

tasks {
    val nats = DockerUtils.dockerContanier(
        project = project,
        image = "nats:2.1.9",
        tcpPorts = listOf(4222 to 8122),
        args = listOf(),
        suffix = "Nats"
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
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
