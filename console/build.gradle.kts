import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.kotlin.clang.eachNative
import pw.binom.useDefault
import java.util.*

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}

fun KotlinNativeTarget.useNativeUtils() {
    compilations["main"].cinterops {
        create("native") {
            defFile = project.file("src/cinterop/native_utils.def")
            packageName = "platform.common"
        }
    }
}

apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    allTargets {
        -"wasm32"
    }
    eachNative {
        useNativeUtils()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }
        useDefault()
        val jvmMain by getting {
            dependencies {
                api("org.jline:jline:3.21.0")
            }
        }
    }
}

fun makeTimeFile() {
    val dateDir = file("$buildDir/tmp-date")
    dateDir.mkdirs()
    val tzFile = file("$dateDir/currentTZ")
    tzFile.delete()
    tzFile.writeText((TimeZone.getDefault().rawOffset / 1000 / 60).toString())
}

tasks {
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinTest::class).forEach {
        it.doFirst {
            makeTimeFile()
        }
    }
}
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
