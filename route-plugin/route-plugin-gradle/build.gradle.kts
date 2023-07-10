plugins {
  id("java-gradle-plugin")
  kotlin("jvm")
  id("com.github.gmazzo.buildconfig")
  id("maven-publish")
}

dependencies {
  implementation(kotlin("gradle-plugin-api"))
}
val routePluginId = project.property("route_plugin.id") as String
buildConfig {
  val project = project(":route-plugin")
  packageName("${project.group}.strong")
  buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$routePluginId\"")
  buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
  buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
  buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
}

gradlePlugin {
  plugins {
    create("kotlinIrPluginTemplate") {
      id = routePluginId
      displayName = "Kotlin Http Server"
      description = displayName
      implementationClass = "pw.binom.strong.StrongGradlePlugin"
      isAutomatedPublishing = true
    }
  }
}

//publishing {
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
//}

apply {
  plugin(pw.binom.plugins.BinomPublishPlugin::class.java)
}
apply<pw.binom.plugins.DocsPlugin>()
