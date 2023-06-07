package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

abstract class OpenSSLUnpackTask : DefaultTask() {

    @get:InputFile
    abstract val input: RegularFileProperty

    @get:OutputDirectory
    abstract val output: RegularFileProperty

    init {
        output.set(project.buildDir.resolve("openssl/source"))
    }

    @TaskAction
    fun execute() {
        val fileZip = input.get().asFile
        val destDir = output.get().asFile

        val zis = ZipInputStream(fileZip.inputStream())
        var zipEntry: ZipEntry? = zis.nextEntry
        while (zipEntry != null) {
            val p = zipEntry.name.indexOf("/")
            if (p == -1) {
                zipEntry = zis.nextEntry
                continue
            }
//            val newFile = destDir.resolve(zipEntry.name)
            val newFile = destDir.resolve(zipEntry.name.substring(p + 1))
            if (zipEntry.isDirectory) {
                if (!newFile.isDirectory && !newFile.mkdirs()) {
                    throw IOException("Failed to create directory $newFile")
                }
            } else {
                val parent = newFile.parentFile
                if (!parent.isDirectory && !parent.mkdirs()) {
                    throw IOException("Failed to create directory $parent")
                }

                // write file content
                newFile.outputStream().use {
                    zis.copyTo(it)
                }
            }
            zipEntry = zis.nextEntry
        }

        zis.closeEntry()
        zis.close()
    }
}
