package pw.binom

import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType

object Target {
    const val MINGW_X86_SUPPORT = false
    const val LINUX_ARM64_SUPPORT = false
    const val LINUX_ARM32HFP_SUPPORT = false
    const val ANDROID_JVM_SUPPORT = false
    const val MACOS_SUPPORT = true
    val JS_TARGET: KotlinJsCompilerType = KotlinJsCompilerType.IR
}
