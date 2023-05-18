import pw.binom.eachKotlinCompile
import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
fun androidCInterop(target: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget) {
//    target.run {
//        binaries {
//            compilations["main"].cinterops {
//                create("android") {
//                    defFile = project.file("src/androidNativeMain/cinterop/android.def")
//                    packageName = "platform.android"
//                }
//            }
//        }
//    }
}
kotlin {
    allTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib"))
                api(project(":collections"))
            }
//            kotlin.srcDir("build/gen")
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        useDefault()
    }
}

tasks {
//    val generateVersion = create("generateVersion") {
//        val sourceDir = project.buildDir.resolve("gen/pw/binom")
//        sourceDir.mkdirs()
//        val versionSource = sourceDir.resolve("version.kt")
//        outputs.files(versionSource)
//        inputs.property("version", project.version)
//
//        versionSource.writeText(
//            """package pw.binom
//
//const val BINOM_VERSION = "${project.version}"
//""",
//        )
//    }
//    eachKotlinCompile {
//        it.dependsOn(generateVersion)
//    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
