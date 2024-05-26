import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
}
apply<pw.binom.KotlinConfigPlugin>()
kotlin {
  allTargets {
    config()
    -"js"
  }
  applyDefaultHierarchyBinomTemplate()
}
apply<pw.binom.plugins.ConfigPublishPlugin>()
