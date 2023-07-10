plugins {
  id("maven-publish")
  kotlin("jvm")
  kotlin("kapt")
  id("com.github.gmazzo.buildconfig")
  id("org.jetbrains.dokka")
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")

  kapt("com.google.auto.service:auto-service:1.0")
  compileOnly("com.google.auto.service:auto-service-annotations:1.0")

  testImplementation(kotlin("test-junit"))
  testImplementation(project(":http-route"))
  api(project(":url"))
  testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
  testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
}

val routePluginId = project.property("route_plugin.id") as String
buildConfig {
  packageName("$group.route")
  buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$routePluginId\"")
}

// publishing {
//  repositories {
//    mavenLocal()
//  }
//  publications {
//    create<MavenPublication>("maven") {
//      groupId = project.group as String
//      artifactId = project.name
//      version = project.version as String
//
//      from(components["java"])
//    }
//  }
// }

apply {
  plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}
apply<pw.binom.plugins.DocsPlugin>()
