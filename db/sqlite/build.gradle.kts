import pw.binom.kotlin.clang.clangBuildStatic
import pw.binom.kotlin.clang.compileTaskName
import pw.binom.kotlin.clang.eachNative
import pw.binom.publish.*

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("maven-publish")
//  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
//    id("com.android.library")
//  }
}
apply<pw.binom.KotlinConfigPlugin>()
val sqlitePackageName = "platform.internal_sqlite"
kotlin {
  allTargets {
    config()
    -"js"
  }
  eachNative {
    val headersPath = file("${buildFile.parentFile}/src/native")
    val sqliteStaticTask =
      clangBuildStatic(name = "sqlite3", target = this.konanTarget) {
        group = "clang"
        this.konanVersion.set(pw.binom.Versions.KOTLIN_VERSION)
        include(headersPath)
        compileArgs(
          "-DSQLITE_ENABLE_FTS3",
          "-DSQLITE_ENABLE_FTS4",
          "-DSQLITE_ENABLE_FTS5",
          "-DSQLITE_ENABLE_RTREE",
          "-DSQLITE_ENABLE_DBSTAT_VTAB",
//        "-DSQLITE_ENABLE_JSON1",
          "-DSQLITE_ENABLE_RBU",
//        "-DSQLITE_THREADSAFE=1",
          "-DSQLITE_ENABLE_EXPLAIN_COMMENTS",
          "-DSQLITE_ENABLE_COLUMN_METADATA=1",
        )
        compileFile(
          file("${buildFile.parentFile}/src/native/sqlite3.c"),
        )
      }
    tasks.findByName(compileTaskName)?.dependsOn(sqliteStaticTask)
    binaries {
      compilations["main"].cinterops {
        val sqlite by creating {
          defFile = project.file("src/nativeInterop/nativeSqlite3.def")
          packageName = sqlitePackageName
          includeDirs.headerFilterOnly(headersPath.absolutePath)
        }
      }
      val args = listOf("-include-binary", sqliteStaticTask.staticFile.asFile.get().absolutePath)
      compilations["main"].kotlinOptions.freeCompilerArgs = args
      compilations["test"].kotlinOptions.freeCompilerArgs = args
    }
  }
  applyDefaultHierarchyBinomTemplate()
  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib-common"))
      api(project(":core"))
      api(project(":db"))
      api(project(":file"))
      api(project(":concurrency"))
      api(project(":thread"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }

    jvmMain.dependencies {
      api("org.xerial:sqlite-jdbc:3.36.0.3")
    }
    jvmTest.dependencies {
      api(kotlin("test-junit"))
    }
    commonTest.dependencies {
      api(kotlin("test-common"))
      api(kotlin("test-annotations-common"))
      api(project(":network"))
      api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
    }
  }
}

apply<pw.binom.plugins.ConfigPublishPlugin>()
