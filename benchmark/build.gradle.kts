import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import kotlinx.benchmark.gradle.KotlinJvmBenchmarkTarget

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.allopen") version "1.9.24"
  id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
}
apply<pw.binom.KotlinConfigPlugin>()

allOpen {
  annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
  jvm {
    compilations.create("benchmark") { associateWith(compilations["main"]) }
  }
//  js(IR) {
//    nodejs()
//    compilations.create("defaultExecutor") { associateWith(compilations["main"]) }
//    compilations.create("builtInExecutor") { associateWith(compilations["main"]) }
//  }
//  wasm("wasmJs") { nodejs() }
//
//  // Native targets
//  macosX64()
//  macosArm64()
  linuxX64()
//  mingwX64()

  applyDefaultHierarchyTemplate()

  targets.configureEach {
    compilations.configureEach {
      kotlinOptions.allWarningsAsErrors = true
    }
  }

  sourceSets.configureEach {
    languageSettings {
      progressiveMode = true
    }
  }

  sourceSets {
    commonMain {
      dependencies {
//        implementation(project(":kotlinx-benchmark-runtime"))
        api(project(":io"))
        api(project(":core"))
        api(project(":http"))
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")
      }
    }

    val jvmMain by getting

    val commonRunnable by creating {
      dependsOn(commonMain.get())
    }

//    val wasmJsMain by getting
//
//    val jsMain by getting
//    val jsDefaultExecutor by getting {
//      dependsOn(jsMain)
//    }
//    val jsBuiltInExecutor by getting {
//      dependsOn(jsMain)
//    }
//    val nativeMain by getting
  }
}
// Configure benchmark
benchmark {

  configurations {
    val main by getting { // --> jvmBenchmark, jsBenchmark, <native target>Benchmark, benchmark
      iterations = 5 // number of iterations
      iterationTime = 300
      iterationTimeUnit = "ms"
      advanced("jvmForks", 3)
      advanced("jsUseBridge", true)
//      include("Common")
//      include("Jvm")
//      include("JvmMain")
//      include("jvmMain")
//      include("CommonBenchmark")
//      include("JvmBenchmark")
//      include("pw.binom.AsyncLazyTest4")
//      include("AsyncLazyTest5")
//      include(".+")
      reportFormat="csv"
    }
    val byteBuffer by creating {
//      include(".+\\.ByteBufferReallocBenchmark")
      include(".+\\.ByteBufferBenchmark")
    }
    val webSocketEncode by creating {
//      include(".+\\.ByteBufferReallocBenchmark")
      include(".+\\.WebSocketEncodeBenchmark")
    }
  }
  targets {
    register("jvm") {
      this as KotlinJvmBenchmarkTarget
      jmhVersion = "1.21"
    }
    /*
    register("jvm", JvmBenchmarkTarget::class) {
//      this as JvmBenchmarkTarget
      jmhVersion = "1.21"
      println("this::class.java=${this::class.java}")
    }
    */
    register("linuxX64"){
    }
    register("mingwX64")
  }
}
/*


  configurations {


    val params by getting {
      iterations = 5 // number of iterations
      iterationTime = 300
      iterationTimeUnit = "ms"
      include("ParamBenchmark")
      param("data", 5, 1, 8)
      param("unused", 6, 9)
    }

    val fast by getting { // --> jvmFastBenchmark, jsFastBenchmark, <native target>FastBenchmark, fastBenchmark
      include("Common")
      exclude("long")
      iterations = 5
      iterationTime = 300 // time in ms per iteration
      iterationTimeUnit = "ms" // time in ms per iteration
      advanced("nativeGCAfterIteration", true)
    }

    val csv by getting {
      include("Common")
      exclude("long")
      iterations = 1
      iterationTime = 300
      iterationTimeUnit = "ms"
      reportFormat = "csv" // csv report format
    }

    val fork by getting {
      include("CommonBenchmark")
      iterations = 5
      iterationTime = 300
      iterationTimeUnit = "ms"
      advanced("jvmForks", "definedByJmh") // see README.md for possible "jvmForks" values
      advanced("nativeFork", "perIteration") // see README.md for possible "nativeFork" values
    }
  }

  // Setup configurations
  targets {
    // This one matches target name, e.g. 'jvm', 'js',
    // and registers its 'main' compilation, so 'jvm' registers 'jvmMain'
    register("jvm") {
      jmhVersion = "1.21"
    }
    // This one matches source set name, e.g. 'jvmMain', 'jvmTest', etc
    // and register the corresponding compilation (here the 'benchmark' compilation declared in the 'jvm' target)
    register("jvmBenchmark") {
      jmhVersion = "1.21"
    }
    register("jsDefaultExecutor")
    register("jsBuiltInExecutor") {
      jsBenchmarksExecutor = kotlinx.benchmark.gradle.JsBenchmarksExecutor.BuiltIn
    }
    register("wasmJs")

    // Native targets
    register("macosX64")
    register("macosArm64")
    register("linuxX64")
    register("mingwX64")
  }

*/

// Node.js with canary v8 that supports recent Wasm GC changes
rootProject.extensions.findByType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension::class.java)
  ?.apply {
    nodeVersion = "21.0.0-v8-canary202309167e82ab1fa2"
    nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
  }

// Drop this when node js version become stable
rootProject.tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask::class.java)
  .configureEach {
    args.add("--ignore-engines")
  }
