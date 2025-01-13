package pw.binom.io.file

import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.io.*
import pw.binom.url.Path
import pw.binom.url.toPath

abstract class AbstractLocalFileSystem : FileSystem2 {
  data class EntityImpl(
    override val path: Path,
    override val isFile: Boolean,
    override val size: Long, override val fileSystem: FileSystem2,
    override val lastModified: Long,
  ) : FileSystem2.Entity {
    constructor(file: File, fileSystem: FileSystem2) : this(
      isFile = file.isFile,
      size = file.size,
      fileSystem = fileSystem,
      lastModified = file.lastModified,
      path = file.path.toPath,
    )
  }

  protected abstract fun resolve(path: Path): File

  override suspend fun getQuota(path: Path): FileSystem2.Quota? {
    val file = resolve(path).takeIfExist() ?: return null

    return FileSystem2.Quota(
      availableBytes = file.freeSpace,
      usedBytes = file.freeSpace - file.availableSpace
    )
  }

  override suspend fun getEntries(path: Path): List<FileSystem2.Entity>? {
    val file = resolve(path).takeIfDirection() ?: return null
    return file.list().map {
      EntityImpl(file = it, fileSystem = this)
    }
  }

  override suspend fun readFile(path: Path, range: FileSystem2.Range): AsyncInput? =
    readFile(resolve(path), range)?.asyncInput()

  override suspend fun writeFile(path: Path, override: Boolean): AsyncOutput =
    writeFile(path = resolve(path), override = override).asyncOutput()

  override suspend fun appendFile(path: Path): AsyncOutput {
    val file = resolve(path)
    if (!file.isFile) {
      throw FileSystem2.EntityExistException("File $path doesn't exist")
    }
    return file.openWrite(append = true).asyncOutput()
  }

  override suspend fun makeDirectories(path: Path): FileSystem2.Entity {
    val e = resolve(path).mkdirs() ?: throw FileSystem2.FSException("Error on folder creating")
    return EntityImpl(file = e, fileSystem = this)
  }

  override suspend fun getEntity(path: Path): FileSystem2.Entity? =
    resolve(path).takeIfExist()?.let { EntityImpl(file = it, fileSystem = this) }

  override suspend fun delete(path: Path, recursive: Boolean) {
    delete(path = resolve(path), recursive = recursive)
  }

  companion object {
    fun delete(path: File, recursive: Boolean) {
      val file = path.takeIfExist() ?: return
      if (recursive) {
        file.deleteRecursive()
      } else {
        if (file.isFile) {
          file.delete()
        } else {
          throw FileSystem2.FSException("Can't remove folder not recursive")
        }
      }
    }

    fun between(value: ULong, min: ULong, max: ULong): ULong =
      when {
        value > max -> max
        value < min -> min
        else -> value
      }

    fun between(value: Long, min: Long, max: Long): Long =
      between(
        value = value.toULong(),
        min = min.toULong(),
        max = max.toULong()
      ).toLong()

    fun readFile(path: File, range: FileSystem2.Range): Input? {
      val file = path.takeIfFile() ?: return null
      val fileStream = file.openRead()
      var stream: Input = fileStream
      fun between(value: Long) =
        between(
          value = value,
          min = 0,
          max = fileStream.size,
        )
      when (range) {
        is FileSystem2.Range.Last -> fileStream.position = between(fileStream.size - range.size)

        is FileSystem2.Range.First -> fileStream.position =
          between(value = range.start)

        is FileSystem2.Range.Between -> {
          val start = between(range.start)
          val end = between(range.end)
          require(end >= start) { "End should be greater or equal than start" }

          fileStream.position = start
          stream = fileStream.withLimit(end - start)
        }
      }
      return stream
    }

    fun writeFile(path: File, override: Boolean): Output {
      val file = path
      if (file.isDirectory) {
        throw FileSystem2.FSException("Can't write data into directory")
      }
      val parent = file.parentOrNull
      if (parent != null && !parent.isDirectory) {
        throw FileSystem2.FSException("Parent directory of $path not exist")
      }

      if (file.isFile && !override) {
        throw FileSystem2.EntityExistException("File $path doesn't exist")
      }
      return file.openWrite(append = false)
    }
  }
}
