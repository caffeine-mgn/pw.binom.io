@file:JvmName("FileJvm")

package pw.binom.io.file

import java.io.File as JFile

actual class File actual constructor(path: String) {

    internal val native = JFile(path)

    actual constructor(parent: File, name: String) : this(
        "${
        parent.path.removeSuffix("/").removeSuffix("\\")
        }$SEPARATOR${name.removePrefix("/").removePrefix("\\")}"
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
            get() = File(System.getProperty("java.io.tmpdir")).takeIfDirection()
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
        val out = ArrayList<File>()
        iterator().forEach { file ->
            out += file
        }

        return out
    }

    actual val freeSpace: Long
        get() = JFile(path).freeSpace

    actual val availableSpace: Long
        get() = JFile(path).usableSpace

    actual val totalSpace: Long
        get() = JFile(path).totalSpace
}

val JFile.binom: File
    get() = File(absolutePath)

val File.java: JFile
    get() = JFile(path)
