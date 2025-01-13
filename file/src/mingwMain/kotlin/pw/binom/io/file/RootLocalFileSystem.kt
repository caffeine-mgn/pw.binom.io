package pw.binom.io.file

import pw.binom.io.FileSystem2
import pw.binom.url.Path

actual object RootLocalFileSystem : FileSystem2, AbstractLocalFileSystem() {

  override fun resolve(path: Path): File {
    val root = path.root
    val f = File("$root:\\")

    return path.removeRoot()?.let { f.relative(it) } ?: f
  }

  actual override suspend fun getEntries(path: Path): List<FileSystem2.Entity>? {
    if (path.isEmpty) {
      return File.listRoots.map {
        EntityImpl(
          file = it,
          fileSystem = this
        )
      }
    }
    return super.getEntries(path)
  }
}
