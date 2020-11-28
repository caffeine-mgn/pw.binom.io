package pw.binom.krpc

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import pw.binom.io.file.*
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.krpc.generation.csharp.CsharpGenerator
import java.io.File

open class KRpcCsharp : DefaultTask() {
    @OutputDirectory
    var destination: File? = null

    @get:InputFiles
    val inputs = ArrayList<File>()

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
        val files = inputs.map { file ->
            file.asBFile.inputStream!!.use {
                val proto = parseProto(it.utf8Reader())
                genProto(proto, pw.binom.io.file.File(destination!!.asBFile, file.nameWithoutExtension + ".cs").asJFile)
            }
        }
    }
}

private fun genProto(file: ProtoFile, outputFile: File) {
    val ff = outputFile.asBFile
    ff.parent?.mkdirs()
    ff.delete()
    CsharpGenerator.generate(file, outputFile.asBFile)
}

fun main() {
    val task = KRpcCsharp()

    task.destination = File("D:\\WORK\\slidearena\\server\\game\\build\\gen")
    task.from(listOf(
            File("D:\\WORK\\slidearena\\server\\game\\src\\proto\\dto.proto"),
            File("D:\\WORK\\slidearena\\server\\game\\src\\proto\\services.proto")
    ))
    task.action()
}