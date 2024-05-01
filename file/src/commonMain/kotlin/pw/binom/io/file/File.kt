package pw.binom.io.file

import pw.binom.url.Path

expect class File(path: String) {
  constructor(parent: File, name: String)

  val path: String
  val size: Long
  val lastModified: Long

  val isFile: Boolean
  val isDirectory: Boolean
  val freeSpace: Long
  val availableSpace: Long
  val totalSpace: Long
  val parent: File
  val parentOrNull: File?
  val nameWithoutExtension: String
  val extension: String?
  val name: String

  fun delete(): Boolean
  fun mkdir(): Boolean
  fun renameTo(newPath: File): Boolean
  fun list(): List<File>

  companion object {
    val SEPARATOR: Char
    val temporalDirectory: File?
  }

  override fun equals(other: Any?): Boolean
  override fun hashCode(): Int
  override fun toString(): String
  fun getPosixMode(): PosixPermissions
  fun setPosixMode(mode: PosixPermissions): Boolean
  fun createSymbolicLink(to: File)
  fun relative(path: String): File
  fun relative(path: Path): File
  fun mkdirs(): File?
  fun deleteRecursive(): Boolean
}

fun String.toFile() = File(this)
