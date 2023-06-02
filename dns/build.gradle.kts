import org.jetbrains.kotlin.konan.target.HostManager
import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    jvm()
    linuxX64()
//    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
//        linuxArm32Hfp()
//    }
    mingwX64()
//    if (pw.binom.Target.MINGW_X86_SUPPORT) {
//        mingwX86()
//    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    if (HostManager.hostIsMac) {
        macosX64()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
                api(project(":network"))
                api("pw.binom:bitarray:0.2.2")
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
