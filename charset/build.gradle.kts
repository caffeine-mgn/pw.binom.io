import pw.binom.publish.dependsOn

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
    jvm()
    linuxX64()
//    linuxArm64()
//    linuxArm32Hfp()
//    linuxMips32()
//    linuxMipsel32()
    mingwX64()
//    mingwX86()
    macosX64()
//    macosArm64()
//    iosX64()
//    iosArm32()
//    iosArm64()
//    iosSimulatorArm64()
//    watchosX64()
//    watchosX86()
//    watchosArm32()
//    watchosArm64()
//    watchosSimulatorArm64()
//    androidNativeX64()
//    androidNativeX86()
//    androidNativeArm32()
//    androidNativeArm64()
//    wasm32()

    js(BOTH) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
            }
        }
        val linuxX64Test by getting {
            dependsOn(commonTest)
        }
        dependsOn("linux*Main", linuxX64Main)
        dependsOn("mingw*Main", linuxX64Main)
        dependsOn("watchos*Main", linuxX64Main)
        dependsOn("macos*Main", linuxX64Main)
        dependsOn("ios*Main", linuxX64Main)
        dependsOn("androidNative*Main", linuxX64Main)
        dependsOn("wasm*Main", linuxX64Main)

        dependsOn("linux*Test", linuxX64Test)
        dependsOn("mingw*Test", linuxX64Test)
        dependsOn("watchos*Test", linuxX64Test)
        dependsOn("macos*Test", linuxX64Test)
        dependsOn("ios*Test", linuxX64Test)
        dependsOn("androidNative*Test", linuxX64Test)

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test"))
            }
        }

        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
                api(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
