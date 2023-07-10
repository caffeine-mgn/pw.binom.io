plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("org.jetbrains.dokka")
  id("maven-publish")
}

sourceSets {
  named("main") {
    java.srcDir("build/generated/source/common")
  }
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-compiler")

  kapt("com.google.auto.service:auto-service:1.0")
  compileOnly("com.google.auto.service:auto-service-annotations:1.0")
}

tasks.named("compileKotlin") { dependsOn("syncSource") }
tasks.register<Sync>("syncSource") {
  from(project(":route-plugin:route-plugin").sourceSets.main.get().allSource)
  into("build/generated/source/common")
  filter {
    // Replace shadowed imports from plugin module
    when (it) {
      "import org.jetbrains.kotlin.com.intellij.mock.MockProject" -> "import com.intellij.mock.MockProject"
      else -> it
    }
  }
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
