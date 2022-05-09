package pw.binom.io.file

expect class FileIterator internal constructor(path: File) : Iterator<File>

fun File.iterator() = FileIterator(this)
