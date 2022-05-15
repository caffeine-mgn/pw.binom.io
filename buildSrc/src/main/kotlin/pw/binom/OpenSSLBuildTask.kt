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
        val opensslDir = project.propertyOrNull("pw.binom.openssl-dir")
//        if (opensslDir != null) {
//            opensslDirection.set(File(opensslDir))
//        }

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
        val envs = mapOf(
            "CC" to llvmPath,
            "CXX" to llvmPath,
            "AR" to llvmArPath,
            "ARFLAGS" to "rc",
//            "PATH" to "${System.getenv("PATH")}$PathSeparator${info.llvmDir}"
        )
        val target1 = when (target.get()) {
            KonanTarget.MINGW_X64 -> "mingw64"
            KonanTarget.MINGW_X86 -> "mingw"
            KonanTarget.LINUX_X64 -> "linux-x86_64"
            KonanTarget.LINUX_ARM64 -> "linux-aarch64"
            KonanTarget.ANDROID_ARM32 -> "android-arm"
            KonanTarget.ANDROID_ARM64 -> "android-arm64"
            KonanTarget.ANDROID_X86 -> "android-arm64"
            KonanTarget.ANDROID_X64 -> "android64-x86_64"
            else -> TODO()
        }
//        val target2 = when (target.get()) {
//            KonanTarget.MINGW_X64 -> "x86_64-w64-mingw32"
//            KonanTarget.MINGW_X86 -> "i686-w64-mingw32"
//            KonanTarget.LINUX_X64 -> "x86_64-unknown-linux-gnu"
//            KonanTarget.LINUX_ARM64 -> "aarch64-unknown-linux-gnu"
//            KonanTarget.ANDROID_ARM32 -> "arm-linux-androideabi"
//            KonanTarget.ANDROID_ARM64 -> "aarch64-linux-android"
//            KonanTarget.ANDROID_X86 -> "i686-linux-android"
//            KonanTarget.ANDROID_X64 -> "aarch64-linux-android"
//            else -> TODO()
//        }
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
                }"
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
            Family.MINGW -> "*.obj"
            else -> "*.o"
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
    builder.environment().putAll(System.getenv())
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

val PathSeparator
    get() = when (HostManager.host.family) {
        Family.MINGW -> ";"
        else -> ":"
    }
