import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

fun config(config: KotlinNativeTarget) {
    config.compilations["main"].cinterops {
        create("openssl") {
            defFile = project.file("src/cinterop/openssl.def")
            packageName = "platform.openssl"
            includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
        }
    }

    val args = listOf(
        "-include-binary", "${buildFile.parent}/src/${config.targetName}Main/cinterop/lib/libopenssl.a"
    )
    config.compilations["main"].kotlinOptions.freeCompilerArgs = args
    config.compilations["test"].kotlinOptions.freeCompilerArgs = args
}

kotlin {
    jvm()
    linuxX64 {
        config(this)
//        compilations["main"].cinterops {
//            create("openssl") {
//                defFile = project.file("src/cinterop/openssl.def")
//                packageName = "platform.openssl"
//                includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
//            }
//        }
//        val args = listOf(
//            "-include-binary", "${buildFile.parent}/src/linuxX64Main/cinterop/lib/libopenssl.a"
//        )
//        compilations["main"].kotlinOptions.freeCompilerArgs = args
//        compilations["test"].kotlinOptions.freeCompilerArgs = args
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            config(this)
        }
    }

    mingwX64 {
        config(this)
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 {
            config(this)
        }
    }

    macosX64 {
        config(this)
    }
    targets.all {
        compilations["main"].compileKotlinTask.kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":core"))
                api(project(":network"))
                api(project(":file"))
                api(project(":date"))
                api(project(":concurrency"))
                api("com.ionspin.kotlin:bignum:0.3.6")
            }
        }
        val linuxX64Main by getting {
//            dependsOn(mingwX64Main)
            dependsOn(commonMain)
        }
        val mingwX64Main by getting {
            dependsOn(linuxX64Main)
        }

        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
//            dependsOn(linuxX64Main)
                dependsOn(linuxX64Main)
            }
        }

        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
//            dependsOn(mingwX64Main)
                dependsOn(mingwX64Main)
            }
        }
        val macosX64Main by getting {
//            dependsOn(mingwX64Main)
            dependsOn(linuxX64Main)
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api("org.bouncycastle:bcprov-jdk15on:1.68")
                api("org.bouncycastle:bcpkix-jdk15on:1.68")
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
apply<pw.binom.plugins.ConfigPublishPlugin>()
extensions.getByType(org.jmailen.gradle.kotlinter.KotlinterExtension::class.java).also {
    it.disabledRules = it.disabledRules + arrayOf("import-ordering")
}
// lintKotlin {
// }
tasks {
    register("buildOpenSSLMingwX64", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.MINGW_X64)
    }
    register("buildOpenSSLMingwX86", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.MINGW_X86)
    }
    register("buildOpenSSLLinuxX64", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.LINUX_X64)
    }
    register("buildOpenSSLAndroidX64", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X64)
    }
    register("buildOpenSSLAndroidX86", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X86)
    }
    register("buildOpenSSLAndroidArm32", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM32)
    }
    register("buildOpenSSLAndroidArm64", pw.binom.OpenSSLBuildTask::class.java).configure {
        target.set(org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM64)
    }
}
