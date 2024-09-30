package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.toByteBuffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.ceil

open class ByteArrayOutput(capacity: Int = 512, val capacityFactor: Float = 1.7f) : Output {
  var data = ByteBuffer(capacity)
    private set
  protected var _wrote = 0
  private var closed = false
  private var locked = false

  /**
   * Returns current size of buffer. Buffer can be grown if you call [alloc] or write date more than [capacity]
   */
  val capacity
    get() = data.capacity

  fun clear() {
    _wrote = 0
    data.clear()
    locked = false
  }

  fun trimToSize() {
    ensureUnlocked()
    if (data.capacity != _wrote) {
      val old = this.data
      this.data = this.data.realloc(_wrote)
      old.close()
    }
  }

  fun toByteArray(): ByteArray {
    ensureOpen()
    ensureUnlocked()
    locked = true
    val position = data.position
    val limit = data.limit
    try {
      data.flip()
      return data.toByteArray()
    } finally {
      data.limit = limit
      data.position = position
      locked = false
    }
  }

  private fun ensureOpen() {
    if (closed) {
      throw StreamClosedException()
    }
  }

  fun writeByte(value: Byte) {
    ensureUnlocked()
    alloc(1)
    data.put(value)
    _wrote++
  }

  fun writeInt(value: Int) {
    ensureUnlocked()
    alloc(Int.SIZE_BYTES)
    value.toByteBuffer(data)
    _wrote += Int.SIZE_BYTES
  }

  fun writeFloat(value: Float) = writeInt(value.toBits())

  fun writeDouble(value: Double) = writeLong(value.toBits())

  fun writeLong(value: Long) {
    ensureUnlocked()
    alloc(Long.SIZE_BYTES)
    value.toByteBuffer(data)
    _wrote += Long.SIZE_BYTES
  }

  fun writeShort(value: Short) {
    ensureUnlocked()
    alloc(Short.SIZE_BYTES)
    value.toByteBuffer(data)
    _wrote += Short.SIZE_BYTES
  }

  fun write(
    input: Input,
    blockSize: Int = DEFAULT_BUFFER_SIZE,
  ) {
    while (true) {
      alloc(blockSize)
      val wasRead = input.read(this.data)
      if (wasRead.isNotAvailable) {
        break
      }
      _wrote += wasRead.length
    }
  }

  suspend fun write(
    input: AsyncInput,
    blockSize: Int = DEFAULT_BUFFER_SIZE,
  ) {
    var w = 0
    while (true) {
      if (input.available == 0) {
        break
      }
      val tmpBlockSize = if (input.available > 0) input.available else blockSize
      alloc(tmpBlockSize)
      val p = data.position
      val wasRead = input.read(this.data)
      data.limit = data.capacity
      if (wasRead.isAvailable) {
        w += wasRead.length
      } else {
        break
      }
      data.position = p + wasRead.length

      _wrote += wasRead.length
    }
  }

  fun alloc(size: Int) {
    ensureUnlocked()
    ensureOpen()

    val needWrite = size - (this.data.remaining)

    if (needWrite > 0) {
      val newSize = maxOf(
        ceil(this.data.capacity.let { if (it == 0) 1 else it } * capacityFactor).toInt(),
        this.data.capacity + _wrote + needWrite,
      )
      val old = this.data
      val new = this.data.realloc(newSize)
      new.limit = new.capacity
      this.data = new
      old.close()
    }
  }

  override fun write(data: ByteBuffer): DataTransferSize {
    ensureUnlocked()
    alloc(data.remaining)
    val l = this.data.write(data)
    if (l.isAvailable) {
      _wrote += l.length
    }
    return l
  }

  fun write(
    data: ByteArray,
    offset: Int = 0,
    length: Int = data.size - offset,
  ): Int {
    ensureUnlocked()
    alloc(data.size)
    val l = this.data.write(data, offset = offset, length = length)
    _wrote += l
    return l
  }

  private fun ensureUnlocked() {
    check(!locked) { "ByteBuffer locked" }
  }

  override fun flush() {
    // Do nothing
  }

  override fun close() {
    ensureUnlocked()
    if (closed) {
      return
    }
    closed = true
    data.close()
  }

  val size: Int
    get() = _wrote

  fun unlock(position: Int) {
    clear() // also set locked = false
    _wrote = position
    data.limit = data.capacity
    data.position = position
  }

  /**
   * Lock this ByteBuffer and return [ByteBuffer] with data. Limit and offset sets for actial data in this [ByteBuffer].
   * After call this function you can't modify this storage using his methods.
   * For modify this storage you should call [clear] or [unlock]
   */
  fun lock(): ByteBuffer {
    ensureUnlocked()
    locked = true
    data.flip()
    return data
  }

  @Suppress("OPT_IN_IS_NOT_ENABLED")
  @OptIn(ExperimentalContracts::class)
  inline fun <T> locked(func: (ByteBuffer) -> T): T {
    contract {
      callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val oldPosition = data.position
    try {
      return func(lock())
    } finally {
      unlock(oldPosition)
    }
  }

  /**
   * Removed [size] bytes from start
   */
  fun removeFirst(size: Int) {
    ensureUnlocked()
    require(size <= data.position)
    val p = data.position
    data.position = size
    data.compact()
    _wrote -= size
    data.position = p - size
  }
}
