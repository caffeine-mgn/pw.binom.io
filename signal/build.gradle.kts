plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    -"js"
  }
  applyDefaultHierarchyTemplate()
  sourceSets {
    val posixMain by creating {
      dependsOn(commonMain.get())
    }
    androidNativeMain {
      dependsOn(posixMain)
    }
    linuxMain {
      dependsOn(posixMain)
    }
    appleMain {
      dependsOn(posixMain)
    }
  }
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
