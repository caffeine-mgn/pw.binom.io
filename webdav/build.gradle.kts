import pw.binom.baseStaticLibConfig
import pw.binom.eachKotlinTest
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
    baseStaticLibConfig()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":httpServer"))
                api(project(":xml"))
                api(project(":date"))
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
                api(project(":httpClient"))
                api("pw.binom.io:test-container:${pw.binom.Versions.TEST_CONTAINERS_VERSION}")
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

tasks{
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

//    eachKotlinTest {
//        it.dependsOn(startWebdavServer)
//        it.finalizedBy(destroyWebdavServer)
//    }
}

apply<pw.binom.plugins.DocsPlugin>()