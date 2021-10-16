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
    jvm {
        compilations.all {
            kotlinOptions {
//                jvmTarget = "11"
            }
        }
    }

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
                api(project(":httpServer"))
                api(project(":xml"))
                api(project(":date"))
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