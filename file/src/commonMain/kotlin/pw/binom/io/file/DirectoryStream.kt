package pw.binom.io.file

expect class DirectoryStream internal constructor(path: File) : Iterator<File>

fun File.iterator() = DirectoryStream(this)
