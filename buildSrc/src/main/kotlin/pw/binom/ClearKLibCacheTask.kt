package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.*

val String.firstUpperChar: String
  get() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

val KonanTarget.dirName: String
  get() =
    when (family) {
      Family.ANDROID -> "androidNative"
      else -> this.family.name.lowercase()
    } +
      architecture.toString().lowercase().firstUpperChar

abstract class ClearKLibCacheTask : DefaultTask() {
  @get:Input
  abstract val target: Property<KonanTarget>

  @TaskAction
  fun execute() {
    val dirName = target.get().dirName
//    val targetName = target.get().dirName
//    val directory = targetName[0].lowercase() + targetName.substring(1)
    val v = project.layout.buildDirectory.asFile.get().resolve("classes/kotlin/$dirName/main/klib/${project.name}.klib")
    if (v.isFile) {
      val rr = v.delete()
      logger.lifecycle("File \"$v\" removed $rr")
    } else {
      logger.lifecycle("File \"$v\" not found!")
    }
  }
}
