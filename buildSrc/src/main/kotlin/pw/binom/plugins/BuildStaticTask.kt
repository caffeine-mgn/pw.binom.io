package pw.binom.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


open class BuildStaticTask : DefaultTask() {

    private class Compile(val source: File, val objectFile: File, val args: List<String>?)

    @Input
    var target: KonanTarget = HostManager.host

    private var compiles = ArrayList<Compile>()
    private val includes = ArrayList<File>()

    @Input
    var multiThread = true

    @Input
    val compileArgs = ArrayList<String>()

    @OutputFile
    var staticFile: File? = null

    @Input
    var debugBuild: Boolean = false

    @Input
    var optimizationLevel: Int = 3

    @InputFiles
    val inputSourceFiles = ArrayList<File>()

    @OutputFiles
    val outputObjectFiles = ArrayList<File>()

    fun compileArgs(vararg args: String) {
        this.compileArgs.addAll(args)
    }

    fun include(vararg includes: File) {
        this.includes.addAll(includes)
    }


    @JvmOverloads
    fun compileDir(sourceDir: File, objectDir: File, args: List<String>? = null, filter: ((File) -> Boolean)? = null) {
        sourceDir.list()?.forEach {
            val f = sourceDir.resolve(it)
            if (f.isFile && (f.extension.toLowerCase() == "c" || f.extension.toLowerCase() == "cpp")) {
                if (filter == null || filter(f)) {
                    compileFile(
                        source = f,
                        args = args
                    )
                }
            }

            if (f.isDirectory && (filter == null || filter(f))) {
                compileDir(
                    sourceDir = f,
                    objectDir = objectDir.resolve(it),
                    args = args,
                    filter = filter
                )
            }
        }
    }

    private val nativeObjDir by lazy {
        project.buildDir.resolve("native").resolve("obj")
    }

    @JvmOverloads
    fun compileFile(source: File, args: List<String>? = null) {
        val outFile =
            nativeObjDir.resolve("${source.nameWithoutExtension}_${source.absolutePath.hashCode() + target.hashCode()}.o")
        compiles.add(
            Compile(
                source = source,
                objectFile = outFile,
                args = args
            )
        )
        inputSourceFiles.add(source)
        outputObjectFiles.add(outFile)
    }

    private class CompileResult(val source: File, val code: Int, val result: String)

    @TaskAction
    fun execute() {
        if (staticFile == null)
            throw InvalidUserDataException("Static output file not set")
        if (optimizationLevel < 0 || optimizationLevel > 3)
            throw InvalidUserDataException("Invalid Optimization Level")
        if (!HostManager().isEnabled(target)) {
            throw StopActionException("Target ${target.name} not supported")
        }
        val env = HashMap<String, String>()
        if (HostManager.hostIsMac && target == KonanTarget.MACOS_X64) {
            env["CPATH"] =
                "/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include"
        }
        val osPathSeparator = if (HostManager.hostIsMingw) {
            ';'
        } else {
            ':'
        }
        env["PATH"] = "$llvmBinFolder$osPathSeparator${System.getenv("PATH")}"

        fun runCompile(compile: Compile): CompileResult =
            run {
                val targetInfo = targetInfoMap.getValue(target)

                val args = ArrayList<String>()
                args.add(llvmBinFolder.resolve("clang").absolutePath.executable)
//            args.add(llvmBinFolder.resolve("gcc").absolutePath.executable)
                args.add("-c")
                args.add("-O$optimizationLevel")
                args.add("-Wall")
                args.add("--target=${targetInfo.targetName}")
                args.add("--sysroot=${targetInfo.sysRoot}")
                args.addAll(targetInfo.clangArgs)
                args.addAll(this.compileArgs)
                if (compile.args != null) {
                    args.addAll(compile.args)
                }
                args.add("-o")
                args.add(compile.objectFile.absolutePath)
                args.add(compile.source.absolutePath)
                includes.forEach {
                    args.add("-I${it.absolutePath}")
                }
                val builder = ProcessBuilder(
                    args
                )
                builder.environment().putAll(env)
//                    builder.redirectError(builder.redirectInput())
//                    builder.inheritIO()
                val process = builder.start()

                val stdout = StreamGobbler(process.inputStream)
                val stdin = StreamGobbler(process.errorStream)
                stdout.start()
                stdin.start()
                process.waitFor()
                stdout.join()
                stdin.join()
                if (process.exitValue() == 0) {
                    logger.lifecycle("Compile ${compile.source}: OK, ${targetInfo.targetName}")
                }
                CompileResult(
                    code = process.exitValue(),
                    result = stdout.out.toString() + stdin.out.toString(),
                    source = compile.source
                )
            }


        var threadPool: ExecutorService? = null
        if (multiThread) {
            threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        }
        val errorExist = AtomicBoolean(false)
        val tasks = compiles.mapNotNull {
            if (!it.source.isFile) {
                logger.warn("Compile ${it.source}: Source not found")
                return@mapNotNull null
            }
            if (it.objectFile.isFile && it.objectFile.lastModified() > it.source.lastModified()) {
                logger.info("Compile ${it.source}: UP-TO-DATE")
                return@mapNotNull null
            }
            Callable {
                if (!errorExist.get()) {
                    val c = runCompile(it)
                    if (c.code != 0)
                        errorExist.set(true)
                    c
                } else
                    CompileResult(
                        source = File(""),
                        code = 1,
                        result = ""
                    )
            }
        }

        val results = if (threadPool != null) {
            threadPool.invokeAll(tasks).map { it.get() }
        } else {
            tasks.map {
                it.call()
            }
        }

        results.forEach {
            if (it.code != 0) {
                throw GradleScriptException(
                    "Can't build \"${it.source}\".", RuntimeException(
                        "Output:\n${it.result}"
                    )
                )
            }
        }

        val args = ArrayList<String>()
        args.add(llvmBinFolder.resolve("llvm-ar").absolutePath.executable)
        args.add("rc")
        args.add(staticFile!!.absolutePath)
        compiles.forEach {
            args.add(it.objectFile.name)
        }

        val builder = ProcessBuilder(
            args
        )
        builder.directory(nativeObjDir)
        builder.environment().put("PATH", "$llvmBinFolder;${System.getenv("PATH")}")
        val process = builder.start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw GradleScriptException(
                "Can't execute link static library",
                RuntimeException("Can't link: Linked returns ${process.exitValue()}")
            )
        }
    }

    private val String.executable
        get() = when (HostManager.host.family) {
            Family.MINGW -> "$this.exe"
            else -> this
        }
}