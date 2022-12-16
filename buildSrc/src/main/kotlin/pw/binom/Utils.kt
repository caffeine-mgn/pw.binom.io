package pw.binom

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.publish.dependsOn

fun KotlinMultiplatformExtension.onlyDev(func: () -> Unit) {
}

fun KotlinMultiplatformExtension.onlyBuild(func: () -> Unit) {
}

fun NamedDomainObjectContainer<KotlinSourceSet>.useDefault() {
    fun KotlinSourceSet.dp(other: KotlinSourceSet?): KotlinSourceSet {
        if (other != null) {
            dependsOn(other)
        }
        return this
    }

    fun Pair<KotlinSourceSet, KotlinSourceSet>.dp(other: Pair<KotlinSourceSet?, KotlinSourceSet?>): Pair<KotlinSourceSet, KotlinSourceSet> {
        first.dp(other.first)
        second.dp(other.second)
        return this
    }

    fun dependsOn(target: String, other: Pair<KotlinSourceSet?, KotlinSourceSet?>) {
        other.first?.let {
            dependsOn("${target}Main", it)
        }
        other.second?.let {
            dependsOn("${target}Test", it)
        }
    }

    fun findTarget(name: String) = findByName("${name}Main") to findByName("${name}Test")
    fun createTarget(name: String) = create("${name}Main") to create("${name}Test")

    val common = findTarget("common")
    val jvmLike = createTarget("jvmLike").dp(common)
    val nativeCommonMain = createTarget("nativeCommon").dp(common)
    val nativeRunnableMain = createTarget("nativeRunnable").dp(nativeCommonMain)
    val mingwMain = createTarget("mingw").dp(nativeRunnableMain)
    val posixMain = createTarget("posix").dp(nativeRunnableMain)
    val linuxMain = createTarget("linux").dp(posixMain)
    val darwinMain = createTarget("darwin").dp(posixMain)

    dependsOn("jvm", jvmLike)
    dependsOn("android", jvmLike)
    dependsOn("linux*", linuxMain)
    dependsOn("mingw*", mingwMain)
    dependsOn("watchos*", posixMain)
    dependsOn("macos*", darwinMain)
    dependsOn("ios*", darwinMain)
    dependsOn("androidNative*", posixMain)
    dependsOn("wasm*Main", nativeCommonMain)

    dependsOn("androidMain", jvmLike)

    common.second?.let {
        it.dependencies {
            api(kotlin("test-common"))
            api(kotlin("test-annotations-common"))
        }
    }

    jvmLike.second.let {
        it.dependencies {
            api(kotlin("test"))
        }
    }
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
