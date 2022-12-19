import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.dependsOn

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        id("com.android.library")
    }
}
apply<pw.binom.KotlinConfigPlugin>()
fun config(config: KotlinNativeTarget) {
    config.compilations["main"].cinterops {
        create("openssl") {
            defFile = project.file("src/cinterop/openssl.def")
            packageName = "platform.openssl"
            includeDirs.headerFilterOnly("${buildFile.parent}/src/cinterop/include")
        }
    }

    val libFile = File("${buildFile.parent}/src/${config.targetName}Main/cinterop/lib/libopenssl.a")
    val args = listOf(
        "-include-binary",
        libFile.toString()
    )
    config.compilations["main"].compileKotlinTask.doFirst {
        if (!libFile.isFile) {
            throw RuntimeException("SSL static lib not found: $libFile")
        }
        println("${config.targetName}: $libFile file exist!")
    }
    config.compilations["main"].kotlinOptions.freeCompilerArgs = args
    config.compilations["test"].kotlinOptions.freeCompilerArgs = args
}

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

    macosX64()
    eachNative {

        val headersPath = file("${buildFile.parent}/src/cinterop/include")

        val keccakStaticTask = clangBuildStatic(name = "keccak", target = this.konanTarget) {
            include(headersPath.resolve("keccak"))
            compileArgs.addAll(listOf("-std=c99", "-O3", "-g"))
            compileFile(
                file("${buildFile.parentFile}/src/cinterop/include/keccak/sha3.c")
            )
        }
        tasks.findByName(compileTaskName)?.dependsOn(keccakStaticTask)
        binaries {
            compilations["main"].cinterops {
                val openssl by creating {
                    defFile = project.file("src/cinterop/openssl.def")
                    packageName = "platform.openssl"
                    includeDirs.headerFilterOnly(headersPath.absolutePath)
                }
            }
            val libFile = file("${buildFile.parent}/src/${targetName}Main/cinterop/lib/libopenssl.a")
            val args = listOf(
                "-include-binary",
                libFile.absolutePath,
                "-include-binary",
                keccakStaticTask.staticFile.asFile.get().absolutePath,
                "-opt-in=kotlin.RequiresOptIn"
            )
            compilations["main"].kotlinOptions.freeCompilerArgs = args
            compilations["test"].kotlinOptions.freeCompilerArgs = args
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(project(":core"))
                api(project(":network"))
                api(project(":file"))
                api(project(":date"))
                api(project(":socket"))
                api(project(":concurrency"))
                api("com.ionspin.kotlin:bignum:${pw.binom.Versions.IONSPIN_BIGNUM_VERSION}")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        dependsOn("linux*Main", linuxX64Main)
        dependsOn("mingw*Main", linuxX64Main)
        dependsOn("macos*Main", linuxX64Main)
        /*
        applyAllNative()

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
*/
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
        if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
            val androidMain by getting {
                dependsOn(jvmMain)
            }
            val androidTest by getting {
                dependsOn(jvmTest)
                dependencies {
                    api(kotlin("test-junit"))
                    api(kotlin("test-common"))
                    api(kotlin("test-annotations-common"))
                    api("com.android.support.test:runner:0.5")
                }
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
