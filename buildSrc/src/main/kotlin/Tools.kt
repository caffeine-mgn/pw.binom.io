import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractKotlinNativeTargetPreset
import org.jetbrains.kotlin.konan.target.KonanTarget

class TargetConfig {
    val nativeTargets = ArrayList<String>()

    operator fun String.unaryMinus() {
        nativeTargets -= this
    }

    operator fun KonanTarget.unaryMinus() {
        -this.name
    }
}

fun KotlinMultiplatformExtension.allTargets() {
    allTargets {}
}

fun KotlinMultiplatformExtension.allTargets(func: (TargetConfig.() -> Unit)) {
    val c = TargetConfig()
    presets.forEach {
        if (it is AbstractKotlinNativeTargetPreset<*>) {
            c.nativeTargets += it.name
        }
    }
    c.nativeTargets += "jvm"
    c.nativeTargets += "js"
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        c.nativeTargets += "android"
    }
    func(c)
    c.nativeTargets.forEach {
        when (it) {
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

            "android" -> android {
                publishAllLibraryVariants()
            }

            else -> targetFromPreset(presets.findByName(it) ?: throw GradleException("target $it not found"))
        }
    }
}
