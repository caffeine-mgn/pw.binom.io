import pw.binom.publish.dependsOn
import pw.binom.publish.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":collections"))
            }
        }
        val otherMain by creating {
            dependsOn(commonMain)
        }
        val nativeIconvMain by creating {
            dependsOn(commonMain)
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        dependsOn("js*Main", otherMain)
        dependsOn("wasm*Main", otherMain)
        dependsOn("linux*Main", nativeIconvMain)
        dependsOn("mingw*Main", nativeIconvMain)
        dependsOn("androidNative*Main", otherMain)

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }

        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-js"))
            }
        }
        useDefault()
    }
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
