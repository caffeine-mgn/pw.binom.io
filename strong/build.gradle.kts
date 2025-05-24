import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets{
    config()
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(project(":core"))
      api(project(":logger"))
      api(project(":collections"))
    }
    /*
            val linuxX64Main by getting {
                dependsOn(commonMain)
            }
            if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
                val linuxArm64Main by getting {
                    dependsOn(linuxX64Main)
                }
            }
            if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
                val linuxArm32HfpMain by getting {
                    dependsOn(linuxX64Main)
                }
            }

            val mingwX64Main by getting {
                dependsOn(linuxX64Main)
            }
            if (pw.binom.Target.MINGW_X86_SUPPORT) {
                val mingwX86Main by getting {
                    dependsOn(linuxX64Main)
                }
            }

            val macosX64Main by getting {
                dependsOn(linuxX64Main)
            }
     */
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.coroutines.test)
      }
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
    /*
    val jvmMain by getting {
        dependsOn(commonMain)
    }
    if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
        val androidMain by getting {
            dependsOn(jvmMain)
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
    val jsTest by getting {
        dependencies {
            api(kotlin("test-js"))
        }
    }
     */
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
