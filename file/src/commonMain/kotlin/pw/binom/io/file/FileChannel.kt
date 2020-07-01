package pw.binom.io.file

import pw.binom.io.Channel

enum class AccessType {
    READ, WRITE, CREATE, APPEND
}

expect class FileChannel(file: File, vararg mode: AccessType) : Channel, FileAccess{
    actual fun skip(length: Long): Long
}

fun File.channel(vararg mode: AccessType): FileChannel {
    if (AccessType.CREATE !in mode && !isFile)
        throw FileNotFoundException(path)
    return FileChannel(this, *mode)
}