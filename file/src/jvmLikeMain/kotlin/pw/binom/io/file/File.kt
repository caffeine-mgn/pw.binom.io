@file:JvmName("FileJvm")

package pw.binom.io.file

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableSet
import pw.binom.url.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.io.File as JFile
import java.nio.file.attribute.PosixFilePermission as JvmPosixFilePermission

actual class File actual constructor(path: String) {

  internal val native = JFile(path)

  actual constructor(parent: File, name: String) : this(
    "${
      parent.path.removeSuffix("/").removeSuffix("\\")
    }$SEPARATOR${name.removePrefix("/").removePrefix("\\")}",
  )

  actual val path: String = replacePath(path)
  actual val isFile: Boolean
    get() = native.isFile

  actual val isDirectory: Boolean
    get() = native.isDirectory

  actual companion object {
    actual val SEPARATOR: Char
      get() = JFile.separatorChar
    actual val temporalDirectory: File?
      get() = System.getProperty("java.io.tmpdir")
        ?.removeSuffix(JFile.pathSeparator)
        ?.let { File(it) }
        ?.takeIfDirection()
  }

  actual fun delete() = native.delete()
  actual fun mkdir(): Boolean = native.mkdir()

  actual override fun toString(): String = path
  actual override fun equals(other: Any?): Boolean {
    if (other !is File) return false
    return path == other.path
  }

  actual override fun hashCode(): Int = 31 + path.hashCode()

  actual val size: Long
    get() = native.length()

  actual val lastModified: Long
    get() = native.lastModified()

  actual fun renameTo(newPath: File): Boolean =
    native.renameTo(newPath.native)

  actual fun list(): List<File> {
    val out = defaultMutableList<File>()
    iterator().forEach { file ->
      out += file
    }

    return out
  }

  actual val parent: File
    get() = fileGetParent(this)
  actual val parentOrNull: File?
    get() = fileGetParentOrNull(this)
  actual val nameWithoutExtension: String
    get() = fileGetNameWithoutExtension(this)
  actual val extension: String?
    get() = fileGetExtension(this)
  actual val name: String
    get() = fileGetName(this)

  actual val freeSpace: Long
    get() = JFile(path).freeSpace

  actual val availableSpace: Long
    get() = JFile(path).usableSpace

  actual val totalSpace: Long
    get() = JFile(path).totalSpace

  actual fun getPosixMode(): PosixPermissions {
    val view = Files.getFileAttributeView(JFile(path).toPath(), PosixFileAttributeView::class.java)
      .readAttributes()
      .permissions()
    var output = PosixPermissions(0u)
    if (PosixFilePermission.OTHERS_EXECUTE in view) {
      output += PosixPermissions.OTHERS_EXECUTE
    }
    if (PosixFilePermission.OTHERS_WRITE in view) {
      output += PosixPermissions.OTHERS_WRITE
    }
    if (PosixFilePermission.OTHERS_READ in view) {
      output += PosixPermissions.OTHERS_READ
    }
    if (PosixFilePermission.GROUP_EXECUTE in view) {
      output += PosixPermissions.GROUP_EXECUTE
    }
    if (PosixFilePermission.GROUP_WRITE in view) {
      output += PosixPermissions.GROUP_WRITE
    }
    if (PosixFilePermission.GROUP_READ in view) {
      output += PosixPermissions.GROUP_READ
    }
    if (PosixFilePermission.OWNER_EXECUTE in view) {
      output += PosixPermissions.OWNER_EXECUTE
    }
    if (PosixFilePermission.OWNER_WRITE in view) {
      output += PosixPermissions.OWNER_WRITE
    }
    if (PosixFilePermission.OWNER_READ in view) {
      output += PosixPermissions.OWNER_READ
    }
    return output
  }

  actual fun setPosixMode(mode: PosixPermissions): Boolean {
    val map = defaultMutableSet<JvmPosixFilePermission>()
    if (mode in PosixPermissions.OTHERS_EXECUTE) {
      map += JvmPosixFilePermission.OTHERS_EXECUTE
    }
    if (mode in PosixPermissions.OTHERS_WRITE) {
      map += JvmPosixFilePermission.OTHERS_WRITE
    }
    if (mode in PosixPermissions.OTHERS_READ) {
      map += JvmPosixFilePermission.OTHERS_READ
    }
    if (mode in PosixPermissions.GROUP_EXECUTE) {
      map += JvmPosixFilePermission.GROUP_EXECUTE
    }
    if (mode in PosixPermissions.GROUP_WRITE) {
      map += JvmPosixFilePermission.GROUP_WRITE
    }
    if (mode in PosixPermissions.GROUP_READ) {
      map += JvmPosixFilePermission.GROUP_READ
    }
    if (mode in PosixPermissions.OWNER_EXECUTE) {
      map += JvmPosixFilePermission.OWNER_EXECUTE
    }
    if (mode in PosixPermissions.OWNER_WRITE) {
      map += JvmPosixFilePermission.OWNER_WRITE
    }
    if (mode in PosixPermissions.OWNER_READ) {
      map += JvmPosixFilePermission.OWNER_READ
    }
    Files.setPosixFilePermissions(JFile(path).toPath(), map)
    return true
  }

  actual fun createSymbolicLink(to: File) {
    Files.createSymbolicLink(toPath, to.toPath)
  }

  val toPath
    get() = Paths.get(path)

  actual fun relative(path: String): File = fileGetRelative(this, path)
  actual fun relative(path: Path): File = relative(path.raw)
  actual fun mkdirs(): File? = fileMkdirs(this)
  actual fun deleteRecursive(): Boolean = fileDeleteRecursive(this)
}

val JFile.binom: File
  get() = File(absolutePath)

val File.java: JFile
  get() = JFile(path)
