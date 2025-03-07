package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.gradle.api.provider.Provider
import org.gradle.util.internal.VersionNumber
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import pw.binom.kotlin.clang.CLang
import pw.binom.kotlin.clang.CLangLinker
import pw.binom.kotlin.clang.KonanVersion
import pw.binom.kotlin.clang.konan.clangTarget
import java.io.File

abstract class BuildBluezTask : DefaultTask() {
  @get:Input
  abstract val target: Property<KonanTarget>

  @get:InputDirectory
  abstract val bluezDirection: RegularFileProperty

  @get:OutputFile
  val staticLib: Provider<File>

  init {
    staticLib = bluezDirection.map {
      it.asFile.resolve("lib/.libs/libbluetooth.a")
    }
  }

  val CLang.ld
    get() = clangFile.parentFile.resolve("lld")

  @TaskAction
  fun execute() {
    val config = KonanVersion.findVersion(VersionNumber.parse(Versions.KOTLIN_VERSION))!!
    val compiler = config.getCppCompiler(target.get()) as CLang
    val linker = config.getLinked(target.get()) as CLangLinker

    val exe = if (HostManager.hostIsMingw) ".exe" else ""
    val envs1 = HashMap<String, String>()

    envs1["CC"] = compiler.clangFile.path
    envs1["CXX"] = compiler.clangFile.path
    envs1["CPP"] = compiler.clangFile.path
    envs1["CCLD"] = compiler.ld.path
    envs1["LD"] = compiler.ld.path
    envs1["GCC"] = ""
    envs1["EPREFIX"] = "/tmp/bbb"
    envs1["AR"] = linker.arFile.path
    envs1["LDFLAGS"] = (compiler.args - listOf("-c")).joinToString(" ")
    envs1["CPPFLAGS"] = (compiler.args - listOf("-c")).joinToString(" ")

    val configArgs = mutableListOf(
      "configure",
      "--disable-test",
      "--disable-tools",
      "--disable-monitor",
      "--disable-cups",
      "--disable-mesh",
      "--disable-client", // fix error with readline
      "--disable-systemd",
      "--disable-logger",
      "--enable-external-ell",
      "--enable-static=yes",
      "--disable-shared",
      "--disable-manpages",
      "--disable-hid",
      "--disable-midi",
      "--disable-nfc",
//      "--disable-silent-rules",
      "--enable-library",
      "--with-phonebook=ebook",
      "--disable-hog",
    )
    if (target.get().family == Family.ANDROID) {
      configArgs += "--enable-android"
    }
    if (logger.isDebugEnabled) {
      configArgs += "--disable-silent-rules"
    }
    configArgs += "--host"
    configArgs += target.get().clangTarget
    println("Target: ${target.get().clangTarget}")
    // clean
//    project.exec {
//      it.executable = "make"
//      it.args = listOf("clean")
//      it.workingDir = bluezDirection.asFile.get()
//    }
    // generate configure using bootstrap
    project.exec {
      it.executable = "bash$exe"
      it.args = listOf("bootstrap")
      it.workingDir = bluezDirection.asFile.get()
      it.environment.putAll(envs1)
    }
    println("Args: ${configArgs.joinToString(" ")}")
    envs1.forEach { key, value ->
      println("export $key='$value' \\")
    }
    // configure
    project.exec {
      it.executable = "bash$exe"
      it.args = configArgs
      it.workingDir = bluezDirection.asFile.get()
      it.environment.putAll(envs1)
    }
    // make static library
    project.exec {
      it.executable = "make"
      it.args = listOf("lib/libbluetooth.la", "-j", Runtime.getRuntime().availableProcessors().toString())
      it.workingDir = bluezDirection.asFile.get()
      it.environment.putAll(envs1)
    }
  }
}
