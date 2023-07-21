plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  jvm()
  linuxX64()
  linuxArm32Hfp()
  linuxArm64()
  linuxMips32()
  linuxMipsel32()
  mingwX64()
  mingwX86()
  macosX64()
  macosArm64()
  androidNativeX64()
  androidNativeX86()
  androidNativeArm32()
  androidNativeArm64()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
      }
    }
    val linuxX64Main by getting {
      dependsOn(commonMain)
    }
    val linuxArm32HfpMain by getting {
      dependsOn(linuxX64Main)
    }
    val linuxArm64Main by getting {
      dependsOn(linuxX64Main)
    }
    val linuxMips32Main by getting {
      dependsOn(linuxX64Main)
    }
    val linuxMipsel32Main by getting {
      dependsOn(linuxX64Main)
    }
    val androidNativeX64Main by getting {
      dependsOn(linuxX64Main)
    }
    val androidNativeX86Main by getting {
      dependsOn(linuxX64Main)
    }
    val androidNativeArm32Main by getting {
      dependsOn(linuxX64Main)
    }
    val androidNativeArm64Main by getting {
      dependsOn(linuxX64Main)
    }
    val mingwX64Main by getting {
      dependsOn(commonMain)
    }
    val mingwX86Main by getting {
      dependsOn(mingwX64Main)
    }
    val macosX64Main by getting {
      dependsOn(linuxX64Main)
    }
    val macosArm64Main by getting {
      dependsOn(macosX64Main)
    }
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
      }
    }
    val jvmTest by getting {
      dependsOn(commonTest)
      dependencies {
        api(kotlin("test"))
      }
    }
    val linuxX64Test by getting {
      dependsOn(commonTest)
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
