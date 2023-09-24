import kotlinx.benchmark.gradle.KotlinJvmBenchmarkTarget
import pw.binom.useDefault

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("kotlinx-serialization")
  id("com.bmuschko.docker-remote-api")
  id("org.jetbrains.kotlinx.benchmark") version "0.4.9"
  if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
    id("com.android.library")
  }
  id("org.jetbrains.kotlin.plugin.allopen") version "1.8.21"
}
apply<pw.binom.KotlinConfigPlugin>()

allOpen {
  annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
  allTargets{
    -"js"
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(kotlin("stdlib-common"))
        api(project(":db"))
        api(project(":collections"))
        api(project(":db:db-serialization"))
        api(project(":db:sqlite"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${pw.binom.Versions.KOTLINX_COROUTINES_VERSION}")
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.9")
      }
    }
    useDefault()
  }
}

benchmark {
  println("this.reportsDir=${this.reportsDir}")
  configurations {
    configurations.forEach {
      println("configurations--->$it  ${it.name}")
    }
    get("main").apply { // --> jvmBenchmark, jsBenchmark, <native target>Benchmark, benchmark
      iterations = 5 // number of iterations
      iterationTime = 300
      iterationTimeUnit = "ms"
      advanced("jvmForks", 3)
      advanced("jsUseBridge", true)
//      reportFormat = "csv"
      reportFormat = "text"
    }/*
    create("params") {
      iterations = 5 // number of iterations
      iterationTime = 300
      iterationTimeUnit = "ms"
      include("ParamBenchmark")
      param("data", 5, 1, 8)
      param("unused", 6, 9)
    }
    create("fast") {// --> jvmFastBenchmark, jsFastBenchmark, <native target>FastBenchmark, fastBenchmark
      include("Common")
      exclude("long")
      iterations = 5
      iterationTime = 300 // time in ms per iteration
      iterationTimeUnit = "ms" // time in ms per iteration
      advanced("nativeGCAfterIteration", true)
    }
    create("csv") {
      include("Common")
      exclude("long")
      iterations = 1
      iterationTime = 300
      iterationTimeUnit = "ms"
      reportFormat = "csv" // csv report format
    }
    create("fork"){
      include("CommonBenchmark")
      iterations = 5
      iterationTime = 300
      iterationTimeUnit = "ms"
      advanced("jvmForks", "definedByJmh") // see README.md for possible "jvmForks" values
      advanced("nativeFork", "perIteration") // see README.md for possible "nativeFork" values
    }
    */
  }

  targets {
    // This one matches target name, e.g. 'jvm', 'js',
    // and registers its 'main' compilation, so 'jvm' registers 'jvmMain'
    register("jvm") {
      println("this::class.java.name=${this::class.java.name}")
      this as KotlinJvmBenchmarkTarget
      jmhVersion = "1.21"
    }
    // This one matches source set name, e.g. 'jvmMain', 'jvmTest', etc
    // and register the corresponding compilation (here the 'benchmark' compilation declared in the 'jvm' target)
    register("jvmBenchmark") {
//      jmhVersion = "1.21"
      (this as? KotlinJvmBenchmarkTarget)?.jmhVersion = "1.21"
    }
    register("jsIr")
    register("jsIrBuiltIn") {
//      jsBenchmarksExecutor = JsBenchmarksExecutor.BuiltIn
    }
    register("native")
  }
}

if (pw.binom.Target.ANDROID_JVM_SUPPORT) {
  apply<pw.binom.plugins.AndroidSupportPlugin>()
}
