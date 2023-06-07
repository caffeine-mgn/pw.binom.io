import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()

fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.useNative() {
    compilations["main"].cinterops {
        create("native") {
            defFile = project.file("src/cinterop/native.def")
            packageName = "platform.common"
        }
    }
}

kotlin {
    allTargets {
        -"js"
    }
    eachNative {
        useNative()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":charset"))
            }
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api(project(":concurrency"))
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
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
