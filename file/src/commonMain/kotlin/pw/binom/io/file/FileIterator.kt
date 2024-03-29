package pw.binom.io.file

import pw.binom.io.Closeable

expect class FileIterator internal constructor(path: File) : Iterator<File>

fun File.iterator() = FileIterator(this)