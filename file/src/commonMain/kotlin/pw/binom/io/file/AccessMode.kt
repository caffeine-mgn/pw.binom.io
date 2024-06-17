package pw.binom.io.file

import kotlin.jvm.JvmInline

@JvmInline
value class AccessMode(private val raw: Int) {
  companion object {
    val EMPTY = AccessMode(0b0)
    val READ = AccessMode(0b1)
    val WRITE = AccessMode(0b01)
    val CREATE = AccessMode(0b001)
    val APPEND = AccessMode(0b0001)
  }

  val isEmpty
    get() = raw == 0

  val isNotEmpty
    get() = raw != 0

  val isRead
    get() = raw and 0b1 != 0

  val isWrite
    get() = raw and 0b01 != 0

  val isCreate
    get() = raw and 0b001 != 0

  val isAppend
    get() = raw and 0b0001 != 0

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
}
