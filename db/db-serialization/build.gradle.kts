import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization")
    id("com.bmuschko.docker-remote-api")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
    jvm()
    linuxX64()
    mingwX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
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
                api(project(":collections"))
                api(project(":db:db-serialization-annotations"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":db:sqlite"))
                api(project(":network"))
                api(project(":db:postgresql-async"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        useDefault()
    }
}

tasks {
    val postgresServer = pw.binom.plugins.DockerUtils.dockerContanier(
        project = project,
        image = "postgres:11",
        tcpPorts = listOf(5432 to 6102),
        args = listOf(),
        suffix = "Postgres",
        envs = mapOf(
            "POSTGRES_USER" to "postgres",
            "POSTGRES_PASSWORD" to "postgres",
            "POSTGRES_DB" to "test",
        ),
        healthCheck = "/usr/bin/pg_isready -U postgres",
    )
    postgresServer.create.configure {
        this.attachStdin.set(true)
        this.attachStdout.set(true)
        this.attachStderr.set(true)
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
