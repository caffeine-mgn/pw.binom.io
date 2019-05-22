package pw.binom.io.file

import java.io.File as JFile

actual class File actual constructor(path: String) {

    internal val native = JFile(path)

    actual constructor(parent: File, name: String) : this("${parent.path.removeSuffix("/").removeSuffix("\\")}$SEPARATOR$name")

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

    override fun toString(): String = path
    actual val size: Long
        get() = native.length()

    actual val lastModified: Long
        get() = native.lastModified()

}