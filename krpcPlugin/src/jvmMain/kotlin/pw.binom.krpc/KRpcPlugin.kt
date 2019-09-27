package pw.binom.krpc

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import pw.binom.io.AppendableUTF8
import pw.binom.io.file.*
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.krpc.generation.kotlin.KotlinGenerator
import java.io.File

open class KRpcPlugin : Plugin<Project> {
    override fun apply(project: Project) {
    }
}

open class KRpcTask : DefaultTask() {

    @OutputDirectory
    var destination: File? = null

    @get:Input
    var suspend: Boolean = false

    @get:InputFiles
    val inputs = ArrayList<File>()

    var outputDtoList = "krdp.dto"

    fun from(files: FileCollection) {
        from(files.files)
    }

    fun from(files: Collection<File>) {
        inputs.clear()
        inputs.addAll(files)
    }

    @TaskAction
    open fun action() {
        if (destination == null)
            TODO("destination not set")
        val files = inputs.map {
            it.asBFile.inputStream!!.use {
                val proto = parseProto(it.utf8Reader())
                genProto(proto, suspend, destination!!)
            }
        }

        generateDtoListFile(outputDtoList, destination!!, files)
    }
}

fun generateDtoListFile(outputDtoList: String, genRootDir: File, files: List<ProtoFile>) {
    if (!files.asSequence().map { it.structs }.any())
        return
    val list = outputDtoList.split('.')
    val outFile = File(genRootDir, outputDtoList.replace('.', '/') + ".kt")
    val sb = StringBuilder()
    if (list.size > 1)
        sb.append("package ${list.subList(0, list.lastIndex).joinToString(".")}\n\n")
    sb
            .append("import pw.binom.krpc.SimpleStructLibrary\n")
            .append("\n")
    sb.append("val ${list.last()} = SimpleStructLibrary(listOf(")
    var first = true
    files.forEach { file ->
        file.structs.forEach {
            if (!first) {
                sb.append(",")
            }
            first = false
            sb.append("\n\t\t")

            var name = it.name
            if (file.packageName != null && file.packageName!!.isNotBlank())
                name = "${file.packageName}.$name"
            sb.append(name)
        }
    }
    sb.append("))")
    outFile.parentFile.mkdirs()
    if (outFile.isFile)
        outFile.delete()
    outFile.writeText(sb.toString())
}

fun genProto(file: ProtoFile, suspend: Boolean, genRootDir: File): ProtoFile {
    val outDir = if (file.packageName == null || file.packageName!!.isEmpty())
        genRootDir
    else
        File(genRootDir, file.packageName!!.replace('.', File.separatorChar))
    val gen = KotlinGenerator()
    file.structs.forEach { struct ->
        val outFile = File(outDir, "${struct.name}.kt").asBFile
        outFile.parent?.mkdirs()
        if (outFile.isFile)
            outFile.delete()
        outFile.outputStream!!.use {
            val r = AppendableUTF8(it)
            gen.struct(packageName = file.packageName, struct = struct, output = r)
        }
    }

    file.services.forEach { service ->
        val outFile = File(outDir, "${service.name}.kt").asBFile
        outFile.parent?.mkdirs()
        if (outFile.isFile)
            outFile.delete()
        outFile.outputStream!!.use {
            val r = AppendableUTF8(it)
            gen.service(packageName = file.packageName, service = service, output = r, suspend = suspend)
        }
    }
    return file
}

fun main() {
    val task = KRpcTask()
    task.suspend = true
    task.outputDtoList = "game.remote.DTOList"
    task.destination = File("D:\\WORK\\slidearena\\server\\game\\build\\gen")
    task.from(listOf(
            File("D:\\WORK\\slidearena\\server\\game\\src\\proto\\dto.proto"),
            File("D:\\WORK\\slidearena\\server\\game\\src\\proto\\services.proto")
    ))
    task.action()
}