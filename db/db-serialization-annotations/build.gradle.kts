import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.publish.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    targets.all {
        compilations.findByName("main")?.compileTaskProvider?.configure {
            this.compilerOptions.freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }
    allTargets {
        -"js"
        -KonanTarget.ANDROID_ARM32
        -KonanTarget.ANDROID_ARM64
        -KonanTarget.ANDROID_X64
        -KonanTarget.ANDROID_X86
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
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
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
