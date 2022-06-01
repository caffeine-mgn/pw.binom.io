package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.kotlin.clang.Konan
import pw.binom.kotlin.clang.StreamGobblerAppendable
import pw.binom.kotlin.clang.targetInfoMap
import pw.binom.publish.propertyOrNull
import java.io.File

abstract class OpenSSLBuildTask : DefaultTask() {
    @get:Input
    abstract val target: Property<KonanTarget>

    @get:Optional
    @get:InputDirectory
    abstract val opensslDirection: RegularFileProperty

    init {
        this.group = "openssl"
    }

    @TaskAction
    fun execute() {
        val opensslDirection = if (opensslDirection.isPresent) {
            opensslDirection.get().asFile
        } else {
            project.propertyOrNull("pw.binom.openssl-dir")?.let { File(it) }
        }

        if (opensslDirection == null) {
            throw GradleException("OpenSSL direction not set. Use property pw.binom.openssl-dir or set opensslDirection property for task")
        }
        if (!opensslDirection.isDirectory) {
            throw GradleException("OpenSSL direction \"$opensslDirection\" not found")
        }
        Konan.checkKonanInstalled()
        Konan.checkSysrootInstalled(target.get())
        val info = targetInfoMap[target.get()] ?: TODO()
        val llvmPath = "${info.llvmDir}${File.separator}clang".executable.replace("\\", "/")
        val llvmArPath = "${info.llvmDir}${File.separator}llvm-ar".executable.replace("\\", "/")
        var path = System.getenv("PATH")
            .removeFromPathExecute("clang")
            .removeFromPathExecute("llvm-ar")
            .addPath(info.llvmDir)
        if (target.get().family == Family.ANDROID) {
            val androidSdk = System.getenv("ANDROID_NDK_ROOT")
            val prebuildName = when (HostManager.host.family) {
                Family.MINGW -> "windows-x86_64"
                Family.LINUX -> "linux-x86_64"
                Family.OSX -> "darwin-x86_64"
                else -> TODO()
            }
            path =
                path.addPath(File("$androidSdk${File.separator}toolchains${File.separator}llvm${File.separator}prebuilt${File.separator}$prebuildName${File.separator}bin"))
        }
        val envs = mutableMapOf(
            "CC" to "clang".executable,
            "CXX" to "clang".executable,
            "AR" to "llvm-ar".executable,
            "ARFLAGS" to "rc",
            "PATH" to path,
        )
        if (target.get().family == Family.ANDROID) {
            envs["LDFLAGS"] = "-pie"
            envs["CFLAGS"] = "-fPIE"
        }

        val target1 = when (target.get()) {
            KonanTarget.MINGW_X64 -> "mingw64"
            KonanTarget.MINGW_X86 -> "mingw"
            KonanTarget.LINUX_X64 -> "linux-x86_64"
            KonanTarget.LINUX_ARM64 -> "linux-aarch64"
            KonanTarget.ANDROID_ARM32 -> "linux-generic32"
            KonanTarget.ANDROID_ARM64 -> "linux-generic64"
            KonanTarget.ANDROID_X86 -> "linux-generic32"
            KonanTarget.ANDROID_X64 -> "linux-generic64"
            else -> TODO()
        }
        execute(
            args = listOf(
                "perl".executable,
                "Configure",
                target1,
                "no-zlib",
                "no-zlib-dynamic",
                "no-shared",
                "no-threads",
                "--target=${info.targetName} -O3 ${
                info.sysRoot.map { "\"--sysroot=${it.toString().replace("\\", "/")}\"" }.joinToString(" ")
                }",
            ),
            envs = envs,
            directory = opensslDirection,
        )
        execute(
            args = listOf(
                "make".executable,
                "clean",
            ),
            envs = envs,
            directory = opensslDirection,
        )
        execute(
            directory = opensslDirection,
            args = listOf(
                "make".executable,
                "build_libs",
                "-j",
                Runtime.getRuntime().availableProcessors().toString(),
                "-d",
            ),
            envs = envs,
        )
        val targetOutputDir = project.buildDir.resolve("openssl/${target.get().name}")
        val objectDir = targetOutputDir.resolve("o")
        objectDir.mkdirs()
        execute(
            args = listOf(
                "ar".executable,
                "-x",
                "$opensslDirection${File.separator}libssl.a"
            ),
            directory = objectDir,
            envs = envs,
        )
        execute(
            args = listOf(
                "ar".executable,
                "-x",
                "$opensslDirection${File.separator}libcrypto.a"
            ),
            directory = objectDir,
            envs = envs,
        )

        val objFileExt = when (target.get().family) {
            Family.MINGW -> "*.o"
            else -> "*.o"
        }
        if (target.get() == KonanTarget.MINGW_X64) {
            objectDir.listFiles().forEach {
                it.renameTo(it.parentFile.resolve("${it.nameWithoutExtension}.o"))
            }
        }
        execute(
            args = listOf(
                "ar".executable,
                "-rv",
                "$targetOutputDir${File.separator}libopenssl.a",
                objFileExt
            ),
            directory = objectDir,
            envs = envs,
        )
    }
}

fun execute(args: List<String>, directory: File, envs: Map<String, String>): Int {
    val builder = ProcessBuilder(
        args
    )
    val cmd = args.map { it.replace("\"", "\\\"") }.map { "\"$it\"" }.joinToString(" ")
    println("Executing $cmd")
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
    get() = when (HostManager.host.family) {
        Family.MINGW -> "$this.exe"
        else -> this
    }

internal val String.cmd
    get() = when (HostManager.host.family) {
        Family.MINGW -> "$this.cmd"
        else -> "$this.sh"
    }

val pathSeparator
    get() = when (HostManager.host.family) {
        Family.MINGW -> ";"
        else -> ":"
    }

fun String.removeFromPathExecute(execute: String) =
    split(pathSeparator)
        .filter {
            !File(it).resolve(execute.executable).isFile
        }
        .joinToString(separator = pathSeparator)

fun String.addPath(path: File): String {
    val r = split(pathSeparator).toMutableSet()
    r += path.toString()
    return r.joinToString(separator = pathSeparator)
}
