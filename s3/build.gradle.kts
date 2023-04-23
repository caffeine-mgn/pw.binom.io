import pw.binom.eachKotlinTest
import pw.binom.publish.dependsOn
import pw.binom.useDefault

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
//    mingwX86()
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

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
                api(project(":httpClient"))
                api(project(":core"))
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        useDefault()
    }
}

tasks {
//    val s3Server = pw.binom.plugins.DockerUtils.dockerContanier(
//        project = project,
//        image = "jbergknoff/s3rver",
//        tcpPorts = listOf(5000 to 7122),
//        args = listOf(),
//        suffix = "S3",
//    )

    val s3Server = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
//        image = "bitnami/minio:latest",
        image = "zenko/cloudserver:latest-7.70.7",
        tcpPorts = listOf(8000 to 7122),
        suffix = "S3",
        envs = mapOf(
//            "MINIO_ROOT_USER" to "minio",
//            "MINIO_ROOT_PASSWORD" to "minio123",
//            "MINIO_SERVER_ACCESS_KEY" to "rGIU8vPsmnOx4Prv",
//            "MINIO_SERVER_SECRET_KEY" to "bT6YEZsstWsjXh8fJzZdbXvdFZGp3IbR",
//            "MINIO_DEFAULT_BUCKETS" to "test",
//            "MINIO_SCHEME" to "http",
//            "MINIO_REGION" to "ru-central1",
//            "S3BACKEND" to "mem"
        ),
    )

    eachKotlinTest {
        s3Server.dependsOn(it)
    }
}

apply<pw.binom.plugins.DocsPlugin>()
