package pw.binom.io.file

import kotlin.jvm.JvmInline

@JvmInline
value class AccessMode(val raw: Int) {
  companion object {
    private const val READ_VALUE = 0b0001
    private const val WRITE_VALUE = 0b0010
    private const val CREATE_VALUE = 0b00100
    private const val APPEND_VALUE = 0b01000
    val EMPTY = AccessMode(0b0)
    val READ = AccessMode(READ_VALUE)
    val WRITE = AccessMode(WRITE_VALUE)
    val CREATE = AccessMode(CREATE_VALUE)
    val APPEND = AccessMode(APPEND_VALUE)
  }

  val isEmpty
    get() = raw == 0

  val isNotEmpty
    get() = raw != 0

  val isRead
    get() = (raw and READ_VALUE) != 0

  val isWrite
    get() = (raw and WRITE_VALUE) != 0

  val isCreate
    get() = (raw and CREATE_VALUE) != 0

  val isAppend
    get() = (raw and APPEND_VALUE) != 0

  operator fun plus(value: AccessMode) = AccessMode(raw or value.raw)
  operator fun minus(value: AccessMode) = AccessMode((raw.inv() or value.raw).inv())
  fun withRead() = this + READ
  fun withWrite() = this + WRITE
  fun withCreate() = this + CREATE
  fun withAppend() = this + APPEND

  fun forEach(func: (AccessMode) -> Unit) {
    if (isRead) {
      func(READ)
    }
    if (isWrite) {
      func(WRITE)
    }
    if (isCreate) {
      func(CREATE)
    }
    if (isAppend) {
      func(APPEND)
    }
  }

  override fun toString(): String {
    val sb = StringBuilder("AccessMode(")
    var first = true
    fun append(text: String) {
      if (!first) {
        sb.append(", ")
      }
      sb.append(text)
      first = false
    }
    if (isRead) {
      append("READ")
    }
    if (isWrite) {
      append("WRITE")
    }
    if (isCreate) {
      append("CREATE")
    }
    if (isAppend) {
      append("APPEND")
    }
    sb.append(")")
    return sb.toString()
  }
}
