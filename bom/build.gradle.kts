plugins {
  id("java-platform")
  id("maven-publish")
//  id("java-experiments.bom-publish")
}
apply<pw.binom.plugins.ConfigPublishPlugin>()

publishing {
  publications {
    create<MavenPublication>("myPlatform") {
      from(components["javaPlatform"])
    }
  }
}

fun Project.eachAll(): Sequence<Project> =
  sequence {
    yieldAll(subprojects)
    subprojects.forEach {
      yieldAll(it.eachAll())
    }
  }

dependencies {
  constraints {
    project.rootProject.eachAll()
      .filter { it.name != "benchmark" }
      .filter { it.name != "bom" }
      .forEach {
        api(it)
      }
  }
}
