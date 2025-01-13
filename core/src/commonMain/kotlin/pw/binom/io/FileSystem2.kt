package pw.binom.io

import pw.binom.copyTo
import pw.binom.url.Path
import kotlin.coroutines.cancellation.CancellationException

interface FileSystem2 {

  data class Quota(
    val availableBytes: Long,
    val usedBytes: Long,
  )

  sealed interface Range {
    data class First(val start: Long) : Range {
      init {
        require(start >= 0) { "Start should be greater than start or equal to 0" }
      }
    }

    data class Between(val start: Long, val end: Long) : Range {
      init {
        require(start >= 0) { "Start should be greater than start or equal to 0" }
        require(end >= 0) { "End should be greater than start or equal to 0" }
      }
    }

    data class Last(val size: Long) : Range {
      init {
        require(size >= 0) { "Size should be greater than start or equal to 0" }
      }
    }
  }

  interface Entity {
    val path: Path
    val isFile: Boolean
    val size: Long
    val lastModified: Long
    val fileSystem: FileSystem2
    val name
      get() = path.name
  }

  suspend fun getQuota(path: Path): Quota?
  suspend fun getEntries(path: Path): List<Entity>?
  suspend fun readFile(path: Path, range: Range): AsyncInput?

  /**
   * @throws EntityExistException throws when [path] already exist and [override]`==false`
   * @throws FSException throws when data can't be write to [path]
   * @throws FileNotFoundException throws when directory of [path] not exist
   */
  @Throws(
    FSException::class,
    CancellationException::class,
    FileNotFoundException::class,
    EntityExistException::class,
    ForbiddenException::class
  )
  suspend fun writeFile(path: Path, override: Boolean): AsyncOutput
  suspend fun appendFile(path: Path): AsyncOutput
  suspend fun makeDirectories(path: Path): Entity
  suspend fun getEntity(path: Path): Entity?
  suspend fun delete(path: Path, recursive: Boolean)
  suspend fun move(from: Path, to: Path, override: Boolean) {
    copy(from = from, to = to, override = override)
    delete(from, true)
  }

  suspend fun copy(from: Path, to: Path, override: Boolean) {
    to.parent?.let { makeDirectories(it) }
    val en = getEntity(from) ?: throw FileNotFoundException()
    if (en.isFile) {
      val stream = readFile(from, Range.First(0)) ?: throw FileNotFoundException()
      stream.useAsync { fromStream ->
        writeFile(to, override).useAsync { toStream ->
          fromStream.copyTo(toStream)
        }
      }
    } else {
      getEntries(from)?.forEach {
        copy(
          from = from.append(it.name),
          to = to.append(it.name),
          override = override,
        )
      }
    }
  }

  open class FSException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
  }

  class EntityExistException : FSException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
  }

  class FileNotFoundException : FSException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
  }

  open class ForbiddenException : FSException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
  }
}
