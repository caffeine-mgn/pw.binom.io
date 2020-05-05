package pw.binom.io.file

import pw.binom.io.use

expect class File(path: String) {
    constructor(parent: File, name: String)

    val path: String
    val size: Long
    val lastModified: Long

    val isFile: Boolean
    val isDirectory: Boolean

    fun delete(): Boolean
    fun mkdir(): Boolean
    fun renameTo(newPath: File): Boolean

    companion object {
        val SEPARATOR: Char
    }

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

val File.isExist: Boolean
    get() = isFile || isDirectory

/**
 * Returns current work directory
 */
expect val File.Companion.workDirectory: File

val File.name: String
    get() {
        val p = path.lastIndexOf(File.SEPARATOR)
        if (p == -1)
            return path
        return path.substring(p + 1)
    }

val File.parent: File?
    get() = path.lastIndexOf(File.SEPARATOR).let {
        File(if (it == -1)
            return@let null
        else
            path.substring(0, it)
        )
    }

val File.nameWithoutExtension: String
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return name
            else
                name.substring(0, it)
        }
    }

val File.extension: String
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return ""
            else
                name.substring(it + 1)
        }
    }

internal fun replacePath(path: String): String {
    val invalidSeparator = when (File.SEPARATOR) {
        '/' -> '\\'
        '\\' -> '/'
        else -> throw RuntimeException("Invalid FileSeparator \"${File.SEPARATOR}\"")
    }
    return path.replace(invalidSeparator, File.SEPARATOR)
}

fun File.mkdirs(): Boolean {
    if (isFile)
        return false
    if (isDirectory)
        return true
    if (parent?.mkdirs() == false)
        return false
    return mkdir()
}

fun File.deleteRecursive(): Boolean {
    if (isFile)
        return delete()
    if (isDirectory) {
        iterator().use {
            it.forEach {
                if (!it.deleteRecursive())
                    return false
            }
        }
    }
    return delete()
}