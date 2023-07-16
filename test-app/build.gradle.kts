plugins {
  id("idea")
  id("kotlin-multiplatform")
  id("kotlinx-serialization")
  id("org.hidetake.ssh") version "2.11.2"
}
idea {
  module {
    isDownloadSources = true
  }
}
val nativeEntryPoint = "pw.binom.main"

kotlin {
  mingwX64 { // Use your target instead.
    binaries {
      executable {
        entryPoint = nativeEntryPoint
      }
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib"))
        api(project(":socket"))
        api(project(":thread"))
      }
    }
    val mingwX64Main by getting {
      dependencies {
      }
    }

    val commonTest by getting {
      dependencies {
        api(kotlin("test-common"))
        api(kotlin("test-annotations-common"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
      }
    }
  }
}

tasks {
  val runDebugMingwX64 by creating(Exec::class) {
    group = "run"
    this.dependsOn("linkDebugExecutableMingwX64")
    executable = "wine"
    args = listOf(buildDir.resolve("bin/mingwX64/debugExecutable/test-app.exe").path)
  }
}

/*
remotes {
    create("linux-test") {
        host = "192.168.88.85"
        user = "subochev"
        password = "drovosek"
    }
}

tasks {
    val jarTask = this.getByName("shadowJar") as Jar
    register("deploy") {
        dependsOn(jarTask)
        doLast {
            ssh.run(
                delegateClosureOf<org.hidetake.groovy.ssh.core.RunHandler> {
                    val remote =
                        Remote(
                            hashMapOf<String, Any?>(
                                "host" to "192.168.88.85",
                                "user" to "subochev",
                                "password" to "drovosek",
                                "knownHosts" to org.hidetake.groovy.ssh.connection.AllowAnyHosts.instance,
                            ),
                        )
                    println("remote=$remote")
//                    println("ssh.remotes=${ssh.remotes}")
//                    val remote = (ssh.remotes as Map<*,*>)["linux-test"] as Remote
                    session(
                        remote,
                        delegateClosureOf<org.hidetake.groovy.ssh.session.SessionHandler> {
                            put(
                                hashMapOf(
                                    "from" to jarTask.archiveFile.get().asFile,
                                    "into" to "/home/subochev/client/client.jar",
                                ),
                            )
                        },
                    )
                },
            )
        }
    }
}
*/
