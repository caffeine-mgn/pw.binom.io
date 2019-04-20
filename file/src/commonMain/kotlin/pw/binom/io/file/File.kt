package pw.binom.io.file

expect class File(path: String) {
    constructor(parent: File, name: String)

    val parent: File
    val name: String
    val path: String

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