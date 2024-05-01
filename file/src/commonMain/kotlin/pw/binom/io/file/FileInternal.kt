package pw.binom.io.file

internal fun fileGetRelative(file:File,path: String): File {
  if (path.isEmpty()) {
    return file
  }
//    val s1 = path.indexOf('/')
//    val s2 = path.indexOf('\\')
//    val s3 = path.indexOf(":\\")
//    val s4 = path.indexOf(":/")
  val isAbsolute = path.startsWith("/") || path.startsWith("\\") || ":\\" in path || ":/" in path
  if (isAbsolute) {
    return File(path)
  }

  require(!path.startsWith("/") && !path.startsWith("\\")) { "Invalid Relative Path" }
  val currentPath = file.path.split(File.SEPARATOR).toMutableList()
  val newPath = path.split('/', '\\')
  newPath.forEach {
    when (it) {
      "." -> {
      }

      ".." -> {
        require(currentPath.isNotEmpty()) { "Can't find relative from \"${file.path}\" to \"$path\"" }
        currentPath.removeLast()
      }

      else -> currentPath.add(it)
    }
  }
  return File(currentPath.joinToString(File.SEPARATOR.toString()))
}

internal fun fileDeleteRecursive(file:File): Boolean {
  if (file.isFile) {
    return file.delete()
  }
  if (file.isDirectory) {
    file.iterator().forEach {
      if (!it.deleteRecursive()) {
        return false
      }
    }
  }
  return file.delete()
}

internal fun fileMkdirs(file:File): File? {
  if (file.isFile) {
    return null
  }
  if (file.isDirectory) {
    return file
  }
  if (file.parentOrNull?.mkdirs() == null) {
    return null
  }
  if (!file.mkdir()) {
    return null
  }
  return file
}

internal fun fileGetNameWithoutExtension(file: File) =
  file.name.lastIndexOf('.').let {
    if (it == -1) {
      return file.name
    } else {
      file.name.substring(0, it)
    }
  }

internal fun fileGetExtension(file: File): String? =
  file.name.lastIndexOf('.').let {
    if (it == -1) {
      return null
    } else {
      file.name.substring(it + 1)
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

internal fun fileGetName(file:File):String{
  val p = file.path.lastIndexOf(File.SEPARATOR)
  if (p == -1) {
    return file.path
  }
  return file.path.substring(p + 1)
}

internal fun fileGetParentOrNull(file: File): File? =
  file.path.lastIndexOf(File.SEPARATOR).let {
    File(
      if (it == -1) {
        return@let null
      } else {
        file.path.substring(0, it)
      },
    )
  }

internal fun fileGetParent(file: File) =
  fileGetParentOrNull(file) ?: throw IllegalStateException("Can't get parent directory of ${file.path}")
