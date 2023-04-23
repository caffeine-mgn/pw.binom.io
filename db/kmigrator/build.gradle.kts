import pw.binom.useDefault

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jmailen.kotlinter")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    jvm()
    linuxX64()
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp()
    }
    mingwX64()
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86()
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
        linuxArm64()
    }
    macosX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":db"))
                api(project(":logger"))
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

apply<pw.binom.plugins.ConfigPublishPlugin>()
