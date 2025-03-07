package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URL

abstract class DownloadTask: DefaultTask() {

  @get:OutputFile
  abstract val output: RegularFileProperty

  @get:Input
  abstract val url: Property<String>

  @TaskAction
  fun execute() {
    val outputFile = output.get().asFile
    outputFile.delete()
    outputFile.parentFile.mkdirs()

    val connection = URL(url.get()).openConnection() as HttpURLConnection
    connection.inputStream.use { input ->
      outputFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
  }
}
