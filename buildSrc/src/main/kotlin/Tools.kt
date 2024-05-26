import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.publish.TargetConfig

internal class BuildTarget(val name: String, val preset: String)

fun TargetConfig.config() {
  -"watchosArm64"
}

/*
class TargetConfig {
  internal val nativeTargets = ArrayList<BuildTarget>()

  operator fun String.unaryMinus() {
    val target =
      nativeTargets.find { it.name == this }
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
*/
/*
fun KotlinMultiplatformExtension.allTargets() {
  allTargets {}
}
*/

fun KotlinMultiplatformExtension.linux(func: KotlinNativeTarget.() -> Unit = {}) {
//    linuxArm32Hfp(func)
  linuxX64(func)
  linuxArm64(func)
//    linuxMips32(func)
//    linuxMipsel32(func)
}

fun KotlinMultiplatformExtension.watchos(func: KotlinNativeTarget.() -> Unit = {}) {
  watchosX64(func)
//    watchosX86(func)
  watchosArm32(func)
  watchosArm64(func)
  watchosSimulatorArm64(func)
  watchosDeviceArm64(func)
}

fun KotlinMultiplatformExtension.mingw(func: KotlinNativeTarget.() -> Unit = {}) {
  mingwX64(func)
//    mingwX86(func)
}

fun KotlinMultiplatformExtension.macos(func: KotlinNativeTarget.() -> Unit = {}) {
  macosX64(func)
  macosArm64(func)
}

fun KotlinMultiplatformExtension.ios(func: KotlinNativeTarget.() -> Unit = {}) {
  iosX64(func)
//    iosArm32(func)
  iosArm64(func)
  iosSimulatorArm64(func)
}

fun KotlinMultiplatformExtension.androidNative(func: KotlinNativeTarget.() -> Unit = {}) {
  androidNativeX64(func)
  androidNativeX86(func)
  androidNativeArm32(func)
  androidNativeArm64(func)
}
/*
fun KotlinMultiplatformExtension.applyDefaultHierarchyBinomTemplate() {
  val template =
    KotlinHierarchyTemplate.default.extend {
      common {
        group("jvmLike") {
          withAndroidTarget()
          withJvm()
        }
        group("posix") {
          withApple()
          withLinux()
          withAndroidNative()
        }
        group("runnable") {
          withJvm()
          withAndroidTarget()
          withApple()
          withLinux()
          withMingw()
          withAndroidNative()
        }
      }
    }
  applyHierarchyTemplate(template)
}

@OptIn(ExternalVariantApi::class)
fun KotlinMultiplatformExtension.allTargets(func: (TargetConfig.() -> Unit)) {
  val kotlinTargetPropertyName = "kotlin.jvm.target"
  val kotlinJvmTarget =
    if (project.hasProperty(kotlinTargetPropertyName)) {
      project.property(kotlinTargetPropertyName) as String
    } else {
      "1.8"
    }
  jvm()
  val targetConfig = TargetConfig()
  /*
  presets.forEach {
    if (it is AbstractKotlinNativeTargetPreset<*> && it.konanTarget !in KonanTarget.deprecatedTargets) {
//      if (it.konanTarget.family.isAppleFamily && !HostManager.hostIsMac) {
//        return@forEach
//      }
      targetConfig.nativeTargets += BuildTarget(it.konanTarget.name, it.name)
    }
  }
   */
  targetConfig.nativeTargets += BuildTarget("jvm", "jvm")
  targetConfig.nativeTargets += BuildTarget("js", "js")
  targetConfig.nativeTargets += BuildTarget("androidNativeArm32", "androidNativeArm32")
  targetConfig.nativeTargets += BuildTarget("androidNativeArm64", "androidNativeArm64")
  targetConfig.nativeTargets += BuildTarget("androidNativeX64", "androidNativeX64")
  targetConfig.nativeTargets += BuildTarget("androidNativeX86", "androidNativeX86")
  targetConfig.nativeTargets += BuildTarget("linuxArm64", "linuxArm64")
  targetConfig.nativeTargets += BuildTarget("linuxX64", "linuxX64")
  targetConfig.nativeTargets += BuildTarget("mingwX64", "mingwX64")
  if (HostManager.hostIsMac) {
    targetConfig.nativeTargets += BuildTarget("iosArm64", "iosArm64")
    targetConfig.nativeTargets += BuildTarget("iosSimulatorArm64", "iosSimulatorArm64")
    targetConfig.nativeTargets += BuildTarget("iosX64", "iosX64")
    targetConfig.nativeTargets += BuildTarget("macosArm64", "macosArm64")
    targetConfig.nativeTargets += BuildTarget("macosX64", "macosX64")
    targetConfig.nativeTargets += BuildTarget("tvosArm64", "tvosArm64")
    targetConfig.nativeTargets += BuildTarget("tvosSimulatorArm64", "tvosSimulatorArm64")
    targetConfig.nativeTargets += BuildTarget("watchosArm32", "watchosArm32")
    targetConfig.nativeTargets += BuildTarget("watchosArm64", "watchosArm64")
    targetConfig.nativeTargets += BuildTarget("watchosDeviceArm64", "watchosDeviceArm64")
    targetConfig.nativeTargets += BuildTarget("watchosSimulatorArm64", "watchosSimulatorArm64")
    targetConfig.nativeTargets += BuildTarget("watchosX64", "watchosX64")
  }
//    c.nativeTargets += BuildTarget("wasm", "wasm")
//    c.nativeTargets += "wasm"
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    targetConfig.nativeTargets += BuildTarget("android", "android")
  }
  func(targetConfig)
  targetConfig.nativeTargets.forEach {
    when (it.name) {
      "jvm" ->
        jvm {
          compilations.all {
//            it.kotlinOptions.jvmTarget = kotlinJvmTarget
          }
        }

      "js" ->
        js(KotlinJsCompilerType.IR) {
          browser {
            testTask {
//            useKarma {
//              useFirefoxHeadless()
//            }
            }
          }
          nodejs()
        }

      "wasm" ->
        wasmJs {
          browser()
          nodejs()
          d8()
        }

      "android" ->
        androidTarget {
          publishAllLibraryVariants()
        }

      "androidNativeArm32" -> androidNativeArm32()
      "androidNativeArm64" -> androidNativeArm64()
      "androidNativeX64" -> androidNativeX64()
      "androidNativeX86" -> androidNativeX86()
      "linuxArm64" -> linuxArm64()
      "linuxX64" -> linuxX64()
      "mingwX64" -> mingwX64()
      "iosArm64" -> iosArm64()
      "iosSimulatorArm64" -> iosSimulatorArm64()
      "iosX64" -> iosX64()
      "macosArm64" -> macosArm64()
      "macosX64" -> macosX64()
      "tvosArm64" -> tvosArm64()
      "tvosSimulatorArm64" -> tvosSimulatorArm64()
      "tvosX64" -> tvosX64()
      "watchosArm32" -> watchosArm32()
      "watchosArm64" -> watchosArm64()
      "watchosDeviceArm64" -> watchosDeviceArm64()
      "watchosSimulatorArm64" -> watchosSimulatorArm64()
      "watchosX64" -> watchosX64()

      else -> {
        println("it.preset====>${it.preset}")
        targetFromPreset(presets.findByName(it.preset) ?: throw GradleException("target $it not found"))
      }
    }
  }
}
*/
