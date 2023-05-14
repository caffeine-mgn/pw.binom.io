import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

internal class BuildTarget(val name: String, val preset: String)
class TargetConfig {
    internal val nativeTargets = ArrayList<BuildTarget>()

    operator fun String.unaryMinus() {
        val target = nativeTargets.find { it.name == this }
            ?: throw GradleException("Target \"$this\" not found. Available targets: ${nativeTargets.map { it.name }}")
        nativeTargets.remove(target)
    }

    operator fun KonanTarget.unaryMinus() {
        -this.name
    }

    fun withoutDeprecated() {
        KonanTarget.deprecatedTargets.forEach {
            -it
        }
    }
}

fun KotlinMultiplatformExtension.allTargets() {
    allTargets {}
}

fun KotlinMultiplatformExtension.linux(func: KotlinNativeTarget.() -> Unit = {}) {
    linuxArm32Hfp(func)
    linuxX64(func)
    linuxArm64(func)
    linuxMips32(func)
    linuxMipsel32(func)
}

fun KotlinMultiplatformExtension.watchos(func: KotlinNativeTarget.() -> Unit = {}) {
    watchosX64(func)
    watchosX86(func)
    watchosArm32(func)
    watchosArm64(func)
    watchosSimulatorArm64(func)
    watchosDeviceArm64(func)
}

fun KotlinMultiplatformExtension.mingw(func: KotlinNativeTarget.() -> Unit = {}) {
    mingwX64(func)
    mingwX86(func)
}

fun KotlinMultiplatformExtension.macos(func: KotlinNativeTarget.() -> Unit = {}) {
    macosX64(func)
    macosArm64(func)
}

fun KotlinMultiplatformExtension.ios(func: KotlinNativeTarget.() -> Unit = {}) {
    iosX64(func)
    iosArm32(func)
    iosArm64(func)
    iosSimulatorArm64(func)
}

fun KotlinMultiplatformExtension.androidNative(func: KotlinNativeTarget.() -> Unit = {}) {
    androidNativeX64(func)
    androidNativeX86(func)
    androidNativeArm32(func)
    androidNativeArm64(func)
}

fun KotlinMultiplatformExtension.allTargets(func: (TargetConfig.() -> Unit)) {
    val c = TargetConfig()
    presets.forEach {
        if (it is AbstractKotlinNativeTargetPreset<*>) {
            c.nativeTargets += BuildTarget(it.konanTarget.name, it.name)
        }
    }
    c.nativeTargets += BuildTarget("jvm", "jvm")
    c.nativeTargets += BuildTarget("js", "js")
//    c.nativeTargets += BuildTarget("wasm", "wasm")
//    c.nativeTargets += "wasm"
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        c.nativeTargets += BuildTarget("android", "android")
    }
    func(c)
    c.nativeTargets.forEach {
        when (it.name) {
            "jvm" -> jvm()
            "js" -> js(KotlinJsCompilerType.IR) {
                browser {
                    testTask {
                        useKarma {
                            useFirefoxHeadless()
                        }
                    }
                }
                nodejs()
            }

            "wasm" -> wasm {
                browser()
                nodejs()
                d8()
            }

            "android" -> android {
                publishAllLibraryVariants()
            }

            else -> targetFromPreset(presets.findByName(it.preset) ?: throw GradleException("target $it not found"))
        }
    }
}
