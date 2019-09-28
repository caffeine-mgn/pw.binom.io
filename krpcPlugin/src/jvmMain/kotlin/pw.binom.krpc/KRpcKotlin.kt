package pw.binom.krpc

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import pw.binom.io.AppendableUTF8
import pw.binom.io.file.*
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.krpc.generation.Generator
import pw.binom.krpc.generation.kotlin.KotlinGenerator
import java.io.File

open class KRpcKotlin : DefaultTask() {

    @OutputDirectory
    var destination: File? = null

    @get:InputFiles
    val inputs = ArrayList<File>()

    var outputDtoList = "krdp.dto"

    fun from(files: FileCollection) {
        from(files.files)
    }

    fun from(files: Collection<File>) {
        inputs.addAll(files)
    }

    @TaskAction
    open fun action() {
        if (destination == null)
            throw GradleException("Property \"destination\" not set")
        val files = inputs.map {
            it.asBFile.inputStream!!.use {
                val proto = parseProto(it.utf8Reader())
                genProto(proto, destination!!)
            }
        }

        generateDtoListFile(outputDtoList, destination!!, files)
    }
}

private fun generateDtoListFile(outputDtoList: String, genRootDir: File, files: List<ProtoFile>) {
    if (!files.asSequence().map { it.structs }.any())
        return
    val list = outputDtoList.split('.')
    val outFile = File(genRootDir, outputDtoList.replace('.', '/') + ".kt")
    val sb = StringBuilder()
    if (list.size > 1)
        sb.append("package ${list.subList(0, list.lastIndex).joinToString(".")}\n\n")
    sb
            .append("import pw.binom.krpc.SimpleStructLibrary\n")
            .append("import kotlin.native.concurrent.SharedImmutable\n")
            .append("\n")
            .append("@SharedImmutable\n")
            .append("val ${list.last()} = SimpleStructLibrary(listOf(")
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

private fun genProto(file: ProtoFile, genRootDir: File): ProtoFile {
    val outDir = if (file.packageName == null || file.packageName!!.isEmpty())
        genRootDir
    else
        File(genRootDir, file.packageName!!.replace('.', File.separatorChar))
    val gen: Generator = KotlinGenerator
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
            gen.service(packageName = file.packageName, service = service, output = r)
        }
    }
    return file
}

fun main() {
    val task = KRpcKotlin()
    task.outputDtoList = "game.remote.DTOList"
    task.destination = File("D:\\WORK\\slidearena\\server\\game\\build\\gen")
    task.from(listOf(
            File("D:\\WORK\\slidearena\\server\\game\\src\\proto\\dto.proto"),
            File("D:\\WORK\\slidearena\\server\\game\\src\\proto\\services.proto")
    ))
    task.action()
}