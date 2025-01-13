package pw.binom.io.file

import pw.binom.Environment
import pw.binom.OS
import pw.binom.io.FileSystem2
import pw.binom.os
import pw.binom.url.Path

actual object RootLocalFileSystem : FileSystem2, AbstractLocalFileSystem() {

  override fun resolve(path: Path): File {
    if (Environment.os != OS.WINDOWS) {
      return File(path.raw)
    }
    val root = path.root
    val f = File("$root:\\")

    return path.removeRoot()?.let { f.relative(it) } ?: f
  }

  actual override suspend fun getEntries(path: Path): List<FileSystem2.Entity>? {
    if (path.isEmpty) {
      val list = if (Environment.os != OS.WINDOWS) File("/").list() else File.listRoots
      return list.map {
        EntityImpl(
          file = it,
          fileSystem = this
        )
      }
    }
    return super.getEntries(path)
  }

  actual override suspend fun getEntity(path: Path): FileSystem2.Entity? {
    if (path.isEmpty) {
      return EntityImpl(
        isFile = false,
        fileSystem = this,
        path = path,
        size = 0,
        lastModified = 0,
      )
    }
    return super.getEntity(path)
  }
}
