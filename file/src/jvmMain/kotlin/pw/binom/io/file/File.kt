@file:JvmName("FileJvm")

package pw.binom.io.file

import java.io.File as JFile

actual class File actual constructor(path: String) {

    internal val native = JFile(path)

    actual constructor(parent: File, name: String) : this("${parent.path.removeSuffix("/").removeSuffix("\\")}$SEPARATOR${name.removePrefix("/").removePrefix("\\")}")

    actual val path: String = replacePath(path)
    actual val isFile: Boolean
        get() = native.isFile

    actual val isDirectory: Boolean
        get() = native.isDirectory

    actual companion object {
        actual val SEPARATOR: Char
            get() = JFile.separatorChar
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
}

val java.io.File.asBFile: File
    get() = File(absolutePath)

val File.asJFile: java.io.File
    get() = java.io.File(path)

actual val File.Companion.workDirectory: File
    get() = File(System.getProperty("user.dir"))