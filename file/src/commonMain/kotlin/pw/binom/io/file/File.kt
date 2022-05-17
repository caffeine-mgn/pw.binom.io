package pw.binom.io.file

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.*

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

val File.parent: File?
    get() = path.lastIndexOf(File.SEPARATOR).let {
        File(
            if (it == -1)
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

val File.extension: String?
    get() {
        val name = name
        return name.lastIndexOf('.').let {
            if (it == -1)
                return null
            else
                name.substring(it + 1)
        }
    }

fun File.takeIfExist(): File? {
    if (!isExist) {
        return null
    }
    return this
}

fun File.takeIfDirection(): File? {
    if (!isDirectory) {
        return null
    }
    return this
}

fun File.takeIfFile(): File? {
    if (!isFile) {
        return null
    }
    return this
}

internal fun replacePath(path: String): String {
    val invalidSeparator = when (File.SEPARATOR) {
        '/' -> '\\'
        '\\' -> '/'
        else -> throw RuntimeException("Invalid FileSeparator \"${File.SEPARATOR}\"")
    }
    return path.replace(invalidSeparator, File.SEPARATOR)
}

fun File.mkdirs(): File? {
    if (isFile) {
        return null
    }
    if (isDirectory) {
        return this
    }
    if (parent?.mkdirs() == null) {
        return null
    }
    if (!mkdir()) {
        return null
    }
    return this
}

fun File.deleteRecursive(): Boolean {
    if (isFile)
        return delete()
    if (isDirectory) {
        iterator().forEach {
            if (!it.deleteRecursive())
                return false
        }
    }
    return delete()
}

fun File.relative(path: String): File {
    if (path.startsWith("/") || path.startsWith("\\")) {
        throw IllegalArgumentException("Invalid Relative Path")
    }
    val currentPath = this.path.split(File.SEPARATOR).toMutableList()
    val newPath = path.split('/', '\\')
    newPath.forEach {
        when (it) {
            "." -> {
            }
            ".." -> {
                if (currentPath.isEmpty()) {
                    throw IllegalArgumentException("Can't find relative from \"${this.path}\" to \"$path\"")
                }
                currentPath.removeLast()
            }
            else -> currentPath.add(it)
        }
    }
    return File(currentPath.joinToString(File.SEPARATOR.toString()))
}

fun File.append(text: String, charset: Charset = Charsets.UTF8) {
    openWrite(true).bufferedWriter(charset = charset).use {
        it.append(text)
    }
}

fun File.append(data: ByteArray) {
    openWrite(true).use {
        ByteBuffer.wrap(data).use { buf ->
            it.write(buf)
        }
    }
}

/**
 * Rewrite [text] to current file. If file not exists will create it
 */
fun File.rewrite(
    text: String,
    charset: Charset = Charsets.UTF8,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charBufferSize: Int = bufferSize / 2,
) {
    openWrite(false).bufferedWriter(charset = charset, bufferSize = bufferSize, charBufferSize = charBufferSize).use {
        it.append(text)
    }
}

fun File.rewrite(
    data: ByteArray,
) {
    openWrite(false).use {
        ByteBuffer.wrap(data).use { buf ->
            it.write(buf)
        }
    }
}

fun String.toFile() = File(this)

/**
 * Returns all file content. Reading using [charset]
 */
fun File.readText(
    charset: Charset = Charsets.UTF8,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charBufferSize: Int = bufferSize,
) = openRead()
    .bufferedReader(
        charset = charset,
        bufferSize = bufferSize,
        charBufferSize = charBufferSize,
    )
    .use { it.readText() }

fun File.readBinary(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
): ByteArray {
    ByteArrayOutput().use { out ->
        openRead().use { it.copyTo(out, bufferSize = bufferSize) }
        return out.toByteArray()
    }
}

val Environment.workDirectoryFile
    get() = File(Environment.workDirectory)
