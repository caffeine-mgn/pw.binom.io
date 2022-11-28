import pw.binom.publish.dependsOn

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
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        android {
            publishAllLibraryVariants()
        }
    }
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
//    js(pw.binom.Target.JS_TARGET) {
//        browser()
//        nodejs()
//    }
    targets.all {
        compilations.findByName("main")?.compileKotlinTask?.kotlinOptions?.freeCompilerArgs =
            listOf("-opt-in=kotlin.RequiresOptIn")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
            }
        }

        dependsOn("js*Main", commonMain)
        dependsOn("linux*Main", commonMain)
        dependsOn("mingw*Main", commonMain)
        dependsOn("watchos*Main", commonMain)
        dependsOn("macos*Main", commonMain)
        dependsOn("ios*Main", commonMain)
        dependsOn("androidNative*Main", commonMain)
        dependsOn("wasm*Main", commonMain)

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }

        val macosX64Main by getting {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
    }
}
if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    apply<pw.binom.plugins.AndroidSupportPlugin>()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
