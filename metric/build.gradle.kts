import pw.binom.publish.*

plugins {
  kotlin("multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
apply<pw.binom.plugins.ConfigPublishPlugin>()
kotlin {
  allTargets()
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api("pw.binom:atomic:${pw.binom.Versions.ATOMIC_VERSION}")
    }
    jsMain {
      dependsOn(commonMain.get())
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }

    jsTest.dependencies {
      implementation(kotlin("test-js"))
    }
  }
}
