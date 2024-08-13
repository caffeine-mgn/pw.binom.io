package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.kotlin.clang.CLang
import pw.binom.kotlin.clang.CLangLinker
import pw.binom.kotlin.clang.KonanVersion
import pw.binom.kotlin.clang.StreamGobblerAppendable
import pw.binom.publish.propertyOrNull
import java.io.File

abstract class OpenSSLBuildTask : DefaultTask() {
  @get:Input
  abstract val target: Property<KonanTarget>

  @get:InputDirectory
  abstract val opensslDirection: RegularFileProperty

  @get:OutputFile
  abstract val staticLib: RegularFileProperty

  @get:OutputDirectory
  abstract val tempDirForObjectFiles: RegularFileProperty

  @get:Internal
  abstract val buildDirectory: RegularFileProperty
//    = RegularFile {
//        project.buildDir.resolve("openssl/${target.get().name}/static")
//    }

//  fun afterConfig() {
//    project.fileTree(opensslDirection.get().asFile)
//      .forEach {
//        if (it.endsWith(".h") || it.endsWith(".cpp")) {
//          inputs.file(it)
//        }
//      }
//  }

  init {
    this.group = "openssl"
//        outputs.file(opensslDirection.map { it.asFile.resolve("Makefile") })
    buildDirectory.set(
      target.map { t ->
        RegularFile {
          project.buildDir.resolve("openssl/${t.name}/build")
        }
      },
    )
    staticLib.set(
      target.map { t ->
        RegularFile {
          project.buildDir.resolve("openssl/${t.name}/libopenssl.a")
        }
      },
    )
    tempDirForObjectFiles.set(target.map { t -> RegularFile { project.buildDir.resolve("openssl/${t.name}/static") } })
  }

  @TaskAction
  fun execute() {
    val opensslBuildDir = buildDirectory.get().asFile
    opensslBuildDir.mkdirs()
    val opensslDirection =
      if (opensslDirection.isPresent) {
        opensslDirection.get().asFile
      } else {
        project.propertyOrNull("pw.binom.openssl-dir")?.let { File(it) }
      }

    val config = KonanVersion.findVersion(Versions.KOTLIN_VERSION)!!
    val compiler = config.getCppCompiler(target.get()) as CLang
    val linker = config.getLinked(target.get()) as CLangLinker

    if (opensslDirection == null) {
      throw GradleException("OpenSSL direction not set. Use property pw.binom.openssl-dir or set opensslDirection property for task")
    }
    if (!opensslDirection.isDirectory) {
      throw GradleException("OpenSSL direction \"$opensslDirection\" not found")
    }
    val configName =
      when (target.get()) {
        KonanTarget.MINGW_X64 -> "mingw64"
//        KonanTarget.MINGW_X86 -> "mingw"
        KonanTarget.ANDROID_ARM32 -> "linux-armv4"
        KonanTarget.ANDROID_ARM64,
        KonanTarget.ANDROID_X86,
        KonanTarget.ANDROID_X64,
        KonanTarget.LINUX_X64,
        KonanTarget.LINUX_ARM64,
        -> "linux-x86_64-clang"

        else -> TODO("Not supported yet")
      }
    val exe = if (HostManager.hostIsMingw) ".exe" else ""
    val envs1 = HashMap(System.getenv())
    envs1["CC"] = compiler.file.path
    envs1["CXX"] = compiler.file.path
    envs1["AR"] = linker.file.path
    envs1["CPPFLAGS"] = compiler.args.map { "\"$it\"" }.joinToString(" ")
    if (target.get().family == Family.MINGW) {
      envs1["RC"] = "i686-w64-mingw32-windres"
    } else {
//            envs1["RC"] = linker.file.path
    }
    val configArgs = ArrayList<String>()
    val makeArgs = ArrayList<String>()
    configArgs.addAll(
      listOf(
        "$opensslDirection/Configure",
        configName,
        "no-shared",
        "no-threads",
        "no-ui-console",
        "no-buildtest-c++",
        "no-tests",
        "no-external-tests",
        "--release",
        "no-module",
        "no-dynamic-engine",
        "--static",
        "-static",
        "no-legacy",
        "no-ssl-trace",
      ),
    )
    if (target.get().family == Family.MINGW ||
      target.get().architecture == Architecture.ARM64 ||
      target.get().architecture == Architecture.ARM32
    ) {
      configArgs += "no-asm"
    }
    project.exec {
      it.executable = "perl$exe"
      it.args = configArgs
      it.workingDir = opensslBuildDir
      it.environment.putAll(envs1)
    }

    if (target.get().family == Family.MINGW) {
      makeArgs += "RCFLAGS="
    }
    makeArgs += "PROGRAMS="
    project.exec {
      it.workingDir = opensslBuildDir
      it.executable = "make$exe"
      it.args = listOf("-j", Runtime.getRuntime().availableProcessors().toString()) + makeArgs
      it.environment.putAll(envs1)
    }
//    execute(
//      args = listOf("make$exe", "-j", Runtime.getRuntime().availableProcessors().toString()) + makeArgs,
//      directory = opensslBuildDir,
//      envs = envs1,
//    )
    val temparalFile = tempDirForObjectFiles.get().asFile
    temparalFile.mkdirs()
    linker.extract(
      archive = opensslBuildDir.resolve("libssl.a"),
      outputDirectory = temparalFile,
    )
    linker.extract(
      archive = opensslBuildDir.resolve("libcrypto.a"),
      outputDirectory = temparalFile,
    )
    val staticFile = staticLib.get().asFile
    staticFile.parentFile.mkdirs()
    linker.static(
      objectFiles = temparalFile.listFiles().toList(),
      output = staticFile,
    )
  }
}

fun execute(
  args: List<String>,
  directory: File,
  envs: Map<String, String>,
): Int {
  val builder =
    ProcessBuilder(
      args,
    )
  val cmd = args.map { it.replace("\"", "\\\"") }.map { "\"$it\"" }.joinToString(" ")
  println("Executing $cmd in $directory")
  println("Env: $envs")
//    builder.environment().putAll(System.getenv())
  builder.environment().putAll(envs)
  builder.directory(directory)
  val process = builder.start()
  val stdout = StreamGobblerAppendable(process.inputStream, System.out, false)
  val stderr = StreamGobblerAppendable(process.errorStream, System.err, false)
  stdout.start()
  stderr.start()
  process.waitFor()
  stdout.join()
  stderr.join()
  val exitCode = process.exitValue()
  if (exitCode != 0) {
    throw GradleException("Process ${args.first()} finished with $exitCode exit code")
  }
  return exitCode
}

internal val String.executable
  get() =
    when (HostManager.host.family) {
      Family.MINGW -> "$this.exe"
      else -> this
    }

internal val String.cmd
  get() =
    when (HostManager.host.family) {
      Family.MINGW -> "$this.cmd"
      else -> "$this.sh"
    }

val pathSeparator
  get() =
    when (HostManager.host.family) {
      Family.MINGW -> ";"
      else -> ":"
    }

fun String.removeFromPathExecute(execute: String) =
  split(pathSeparator).filter {
    !File(it).resolve(execute.executable).isFile
  }.joinToString(separator = pathSeparator)

fun String.addPath(path: File): String {
  val r = split(pathSeparator).toMutableSet()
  r += path.toString()
  return r.joinToString(separator = pathSeparator)
}
