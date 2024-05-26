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
    val commonMain by getting {
      dependencies {
//        api(kotlin("stdlib"))
        api(project(":collections"))
      }
    }
    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
      }
    }
  }
}

tasks {
//    val generateVersion = create("generateVersion") {
//        val sourceDir = project.buildDir.resolve("gen/pw/binom")
//        sourceDir.mkdirs()
//        val versionSource = sourceDir.resolve("version.kt")
//        outputs.files(versionSource)
//        inputs.property("version", project.version)
//
//        versionSource.writeText(
//            """package pw.binom
//
// const val BINOM_VERSION = "${project.version}"
// """,
//        )
//    }
//    eachKotlinCompile {
//        it.dependsOn(generateVersion)
//    }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
