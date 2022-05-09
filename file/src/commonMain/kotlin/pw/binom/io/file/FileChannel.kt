package pw.binom.io.file

import pw.binom.io.Channel

enum class AccessType {
    READ, WRITE, CREATE, APPEND
}

expect class FileChannel(file: File, vararg mode: AccessType) :
    Channel,
    RandomAccess {
    actual fun skip(length: Long): Long
}

fun File.channel(vararg mode: AccessType): FileChannel {
    if (AccessType.CREATE !in mode && !isFile)
        throw FileNotFoundException(path)
    return FileChannel(this, *mode)
}

/**
 * Open file for read
 */
inline fun File.openRead() = channel(AccessType.READ)

/**
 * Open file for write
 *
 * @param append flag for append exists file. Default - false
 */
inline fun File.openWrite(append: Boolean = false) =
    if (append)
        channel(AccessType.WRITE, AccessType.CREATE, AccessType.APPEND)
    else
        channel(AccessType.WRITE, AccessType.CREATE)
