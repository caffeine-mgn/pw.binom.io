import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    allTargets {
        -"js"
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":network"))
                api(project(":ssl"))
                api(project(":http"))
                api(project(":httpClient"))
                api(project(":compression"))
                api(project(":binom-coroutines"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        useDefault()
    }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
