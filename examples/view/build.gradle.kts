import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}

description = "AdminWeb"

kotlin {
    js("js", IR) {
        browser {
            dceTask {
                keep("pw.binom.example.main")
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                api(project(":io"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
            }
        }

        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks {
    val jsBrowserDevelopmentRun by getting(org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack::class) {
        this.devServer?.open = false
    }
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.webpackDevServer.version = "4.0.0-rc.0"
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.webpackCli.version = "4.9.1"
}
