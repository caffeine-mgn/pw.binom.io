package pw.binom.io.file

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.FileSystem2
import pw.binom.url.Path

expect object RootLocalFileSystem : FileSystem2{
  override suspend fun getQuota(path: Path): FileSystem2.Quota?
  override suspend fun getEntries(path: Path): List<FileSystem2.Entity>?
  override suspend fun readFile(path: Path, range: FileSystem2.Range): AsyncInput?
  override suspend fun writeFile(path: Path, override: Boolean): AsyncOutput
  override suspend fun appendFile(path: Path): AsyncOutput
  override suspend fun makeDirectories(path: Path): FileSystem2.Entity
  override suspend fun getEntity(path: Path): FileSystem2.Entity?
  override suspend fun delete(path: Path, recursive: Boolean)
  override suspend fun move(from: Path, to: Path, override: Boolean)
  override suspend fun copy(from: Path, to: Path, override: Boolean)
}
