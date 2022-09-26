package pw.binom

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

fun KotlinMultiplatformExtension.onlyDev(func: () -> Unit) {
}

fun KotlinMultiplatformExtension.onlyBuild(func: () -> Unit) {
}

fun KotlinMultiplatformExtension.applyDev() {
    jvm()
    when {
        HostManager.hostIsLinux -> linuxX64()
        HostManager.hostIsMingw -> mingwX64()
        HostManager.hostIsMac -> macosX64()
    }
}

fun <T : KotlinSourceSet> NamedDomainObjectContainer<T>.applyAllNative() {
    val nativeMain = create("nativeMain")
}

fun TaskContainer.eachKotlinTest(func: (Task) -> Unit) {
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.tasks.KotlinTest }
        .forEach(func)
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest }
        .forEach(func)
}

fun TaskContainer.eachKotlinCompile(func: (Task) -> Unit) {
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile<*> }
        .forEach(func)
    this.mapNotNull { it as? org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile<*, *, *> }
        .forEach(func)
}

fun KotlinMultiplatformExtension.baseStaticLibConfig() {
    this.targets.forEach {
        if (it is KotlinNativeTarget) {
            when (it.konanTarget) {
//                KonanTarget.MACOS_ARM64,
//                KonanTarget.MACOS_ARM64 -> it.binaries.framework()
                KonanTarget.WASM32 -> { /*Do nothing*/
                }
                else -> it.binaries.staticLib()
            }
        }
    }
}
