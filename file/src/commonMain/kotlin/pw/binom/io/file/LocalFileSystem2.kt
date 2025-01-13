package pw.binom.io.file

import pw.binom.io.FileSystem2
import pw.binom.url.Path
import pw.binom.url.toPath

class LocalFileSystem2(val root: File) : AbstractLocalFileSystem() {

  override fun resolve(path: Path): File {
    val newPath = if (path.startsWith("/")) {
      path.toString().removePrefix("/").toPath
    } else {
      path
    }
    return root.relative(newPath)
  }

  override suspend fun getEntity(path: Path) = super.getEntity(path)?.let { fixPath(it) }
  override suspend fun getEntries(path: Path): List<FileSystem2.Entity>? {
    return super.getEntries(path)?.map { fixPath(it) }
  }

  private fun fixPath(e: FileSystem2.Entity): FileSystem2.Entity {
    var p = e.path.raw.removePrefix(root.path)
    if (!p.startsWith("/")) {
      p = "/$p"
    }
    return EntityImpl(
      path = p.toPath,
      isFile = e.isFile,
      size = e.size,
      fileSystem = this,
      lastModified = e.lastModified,
    )
  }
}
