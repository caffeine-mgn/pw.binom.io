package pw.binom.io.file

expect class DirectoryStream internal constructor(path: File) : Iterator<File> {
  override fun hasNext(): Boolean
  override fun next(): File
}

fun File.iterator() = DirectoryStream(this)
