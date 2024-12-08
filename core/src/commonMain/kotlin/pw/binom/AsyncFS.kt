package pw.binom

import pw.binom.io.AsyncInput
import pw.binom.io.Quota
import pw.binom.url.Path

interface FS {
  val isFileLockSupport: Boolean
  val isDirectoryLockSupport: Boolean
}

interface AsyncFS : FS {

  suspend fun <T> read(
    path: Path,
    offset: ULong = 0uL,
    length: ULong? = null,
    func: (AsyncInput) -> T,
  ): T

  suspend fun write(path: Path, offset: ULong = 0uL, output: AsyncInput): Boolean
  suspend fun delete(path: Path)
  suspend fun mkdir(path: Path): FSEntry

  suspend fun isLocked(path: Path): Boolean? = null

  suspend fun lock(path: Path): Boolean = false

  suspend fun unlock(path: Path): Boolean = false
  suspend fun getQuota(path: Path): Quota?
}

interface AsyncFSEntry : FSEntry {
  val fileSystem: AsyncFS
}

interface FSEntry {
  val name: String
  val isFile: String
  val length: Long
  val lastModified: Long
}
