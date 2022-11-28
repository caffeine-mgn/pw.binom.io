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
    mingwX64()
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":ssl"))
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
                api(project(":core"))
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

apply<pw.binom.plugins.DocsPlugin>()
