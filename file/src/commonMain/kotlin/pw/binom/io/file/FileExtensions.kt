package pw.binom.io.file

val File.isExist: Boolean
  get() = isFile || isDirectory

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
