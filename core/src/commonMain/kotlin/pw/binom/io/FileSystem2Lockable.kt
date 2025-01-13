package pw.binom.io

import pw.binom.url.Path

interface FileSystem2Lockable : FileSystem2 {
  fun lock(path: Path): Unit
  fun unlock(path: Path): Unit

  class FileLocked : FileSystem2.ForbiddenException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
  }
}
