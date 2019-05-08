package pw.binom.io.file

expect class File(path: String) {
    constructor(parent: File, name: String)

    val path: String
    val size: Long
    val lastModified:Long

    val isFile: Boolean
    val isDirectory: Boolean

    fun delete(): Boolean
    fun mkdir(): Boolean

    companion object {
        val SEPARATOR: Char
    }
}

val File.isExist: Boolean
    get() = isFile || isDirectory


val File.name: String
    get() {
        val p = path.lastIndexOf(File.SEPARATOR)
        if (p == -1)
            return path
        return path.substring(p + 1)
    }

val File.parent: File
    get() = path.lastIndexOf(File.SEPARATOR).let {
        File(if (it == -1)
            ""
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

internal fun replacePath(path: String): String {
    val invalidSeparator = when (File.SEPARATOR) {
        '/' -> '\\'
        '\\' -> '/'
        else -> throw RuntimeException("Invalid FileSeparator \"${File.SEPARATOR}\"")
    }
    return path.replace(invalidSeparator, File.SEPARATOR)
}