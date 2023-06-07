
import pw.binom.publish.useDefault

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
    allTargets {
        -"js"
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":ssl"))
            }
        }
        useDefault()
    }
}

apply<pw.binom.plugins.DocsPlugin>()
