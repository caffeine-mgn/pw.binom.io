package pw.binom.io.file

import java.io.File as JFile

actual class File actual constructor(path: String) {

    internal val native = JFile(path)

    actual constructor(parent: File, name: String) : this("${parent.path}$SEPARATOR$name")

    actual val parent: File
        get() = File(native.parent)

    actual val name: String
        get() = native.name

    actual val path: String = path
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

}