import pw.binom.eachKotlinTest

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("kotlinx-serialization")
}

apply {
  plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}

kotlin {
  jvm()
  linuxX64()
  if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
    linuxArm32Hfp()
  }
  mingwX64()
  if (pw.binom.Target.MINGW_X86_SUPPORT) {
    mingwX86()
  }
  if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
    linuxArm64()
  }
  macosX64()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":core"))
        api(project(":db"))
        api(project(":file"))
        api(project(":date"))
        api(project(":ssl"))
        api(libs.kotlinx.serialization.core)
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.serialization.protobuf)
        api(libs.kotlinx.serialization.properties)
      }
    }

    val linuxX64Main by getting {
      dependsOn(commonMain)
    }
    if (pw.binom.Target.LINUX_ARM64_SUPPORT) {
      val linuxArm64Main by getting {
        dependsOn(commonMain)
      }
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
      val linuxArm32HfpMain by getting {
        dependsOn(commonMain)
      }
    }

    val mingwX64Main by getting {
      dependsOn(commonMain)
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
      val mingwX86Main by getting {
        dependsOn(commonMain)
      }
    }

    val macosX64Main by getting {
      dependsOn(commonMain)
    }

    val jvmMain by getting {
      dependsOn(commonMain)
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api(libs.kotlinx.coroutines.test)
      }
    }
    val jvmTest by getting {
      dependencies {
        api(kotlin("test"))
      }
    }
  }
}

apply<pw.binom.plugins.DocsPlugin>()
tasks {
  withType(Test::class) {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging.showCauses = true
    testLogging.showExceptions = true
    testLogging.showStackTraces = true
    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
  }
}
