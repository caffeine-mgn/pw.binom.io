import pw.binom.publish.dependsOn

plugins {
    id("org.jetbrains.kotlin.multiplatform")
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
    linuxArm64()
    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    mingwX64()
    mingwX86()
    macosX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm32()
    iosArm64()
    iosSimulatorArm64()
    tvos()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosX64()
    watchosX86()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
    wasm32()
    js(pw.binom.Target.JS_TARGET) {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":atomic"))
            }
        }
        val commonRunnableMain by creating {
            dependsOn(commonMain)
        }

        val linuxX64Main by getting {
            dependsOn(commonRunnableMain)
        }

        dependsOn("linux*Main", commonRunnableMain)
        dependsOn("mingw*Main", commonRunnableMain)
        dependsOn("watchos*Main", commonRunnableMain)
        dependsOn("macos*Main", commonRunnableMain)
        dependsOn("ios*Main", commonRunnableMain)
        dependsOn("androidNative*Main", commonRunnableMain)

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependsOn(commonRunnableMain)
        }
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
            }
        }
        dependsOn("androidMain", jvmMain)
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
