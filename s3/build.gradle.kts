import pw.binom.eachKotlinTest
import pw.binom.publish.dependsOn

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization")
    id("com.bmuschko.docker-remote-api")
    id("maven-publish")
}

apply {
    plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
    jvm()

    linuxX64()

//    linuxArm32Hfp()
//    linuxArm64 {
//        binaries {
//            staticLib()
//        }
//    }

    mingwX64()
    mingwX86()
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":httpServer"))
                api(project(":xml"))
                api(project(":date"))
                api(project(":collections"))
                api(project(":xml:xml-serialization"))
            }
        }
        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        dependsOn("linux*Main", linuxX64Main)
        dependsOn("mingw*Main", linuxX64Main)
        dependsOn("watchos*Main", linuxX64Main)
        dependsOn("macos*Main", linuxX64Main)
        dependsOn("ios*Main", linuxX64Main)
        dependsOn("androidNative*Main", linuxX64Main)

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
                api(project(":httpClient"))
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
    val s3Server = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
        image = "jbergknoff/s3rver",
        tcpPorts = listOf(5000 to 7122),
        args = listOf(),
        suffix = "S3",
    )

    eachKotlinTest {
        s3Server.dependsOn(it)
    }
}

apply<pw.binom.plugins.DocsPlugin>()
