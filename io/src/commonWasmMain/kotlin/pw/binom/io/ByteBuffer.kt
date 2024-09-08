package pw.binom.io

import kotlin.time.ExperimentalTime


actual open class ByteBuffer actual constructor(
  /**
   * Access to native memory
   */
  val array: ByteArray,
) : Channel, Buffer, ByteBufferProvider {
  actual companion object;

  actual constructor(size: Int) : this(ByteArray(size))

  actual override val capacity: Int
    get() = array.size
  actual override val hasRemaining: Boolean
    get() = remaining > 0

  actual override val elementSizeInBytes: Int
    get() = 1

  @PublishedApi
  internal var closed = false

  private var _position = 0 // = AtomicInt(0)
  private var _limit = array.size // = AtomicInt(data.capacity)

  actual override val remaining: Int
    get() = _limit - _position

  actual override var position: Int
    get() = _position
    set(value) {
      require(value in 0.._limit) { "Position should be in range between 0 and limit. limit: $_limit, new position: $value" }
      _position = value
    }

  actual override var limit: Int
    get() {
      return _limit
    }
    set(value) {
      if (value > capacity || value < 0) throw createLimitException(value)
//            println("ByteBuffer@${hashCode()}->limit=${_limit.getValue()}->$value")
      _limit = value
      if (position > value) {
        position = value
      }
    }

  actual open val isClosed: Boolean
    get() = closed

  init {
    ByteBufferMetric.inc(this)
    ByteBufferAllocationCallback.onCreate(this)
  }

  @PublishedApi
  internal actual open fun ensureOpen() {
    if (closed) {
      throw ClosedException()
    }
  }

  private fun createLimitException(newLimit: Int): IllegalArgumentException {
    val msg = if (newLimit > capacity) {
      "newLimit > capacity: ($newLimit > $capacity)"
    } else { // assume negative
      require(newLimit < 0) { "newLimit expected to be negative" }
      "newLimit < 0: ($newLimit < 0)"
    }
    return IllegalArgumentException(msg)
  }

  fun isReferenceAccessAvailable(position: Int = this._position) = capacity != 0 && position < capacity

  actual override fun flip() {
    ensureOpen()
    limit = position
    position = 0
  }

  actual fun skip(length: Long): Long = skip(length.toInt()).toLong()

  actual fun skip(length: Int): Int {
    if (closed) {
      return 0
    }
    if (length <= 0) {
      return 0
    }

    val pos = minOf(position + length, limit)
    val len = pos - position
    position = pos
    return len
  }

  actual override fun read(dest: ByteBuffer): DataTransferSize = DataTransferSize.ofSize(readInto(dest))

  actual override fun write(data: ByteBuffer): DataTransferSize = DataTransferSize.ofSize(data.readInto(this))

  actual override fun flush() {

  }

  actual override fun close() {
    if (!closed) {

      preClose()
      closed = true
      ByteBufferMetric.dec(this)
      ByteBufferAllocationCallback.onFree(this)
    }
  }

  actual operator fun get(index: Int): Byte {
    ensureOpen()
    return array[index]
//        return data.access { it[index] }
  }

  actual fun forEach(func: (Byte) -> Unit) {
    ensureOpen()
    internalForEachIndexed { _, value ->
      func(value)
    }
  }

  actual fun forEachIndexed(func: (index: Int, value: Byte) -> Unit) {
    ensureOpen()
    internalForEachIndexed { index, value ->
      func(index, value)
    }
  }

  actual fun indexOfFirst(predicate: (Byte) -> Boolean): Int {
    ensureOpen()
    internalForEachIndexed { index, value ->
      if (predicate(value)) {
        return index
      }
    }
    return -1
  }

  actual fun replaceEach(func: (Int, Byte) -> Byte) {
    ensureOpen()
    internalForEachIndexed { index, value ->
      val newValue = func(index, value)
      array[index] = newValue
    }
  }

  actual operator fun set(index: Int, value: Byte) {
    ensureOpen()
    array[index] = value
  }

  actual fun getByte(): Byte {
    ensureOpen()
    val p = _position
    if (p >= limit) throw EOFException()
    _position = p + 1
    return array[p]
//        return data.access { it[p] }
//        return data[p]
  }

  private inline fun internalForEachIndexed(func: (Int, Byte) -> Unit) {
    val start = _position
    val end = _limit
    var cursor = start
    while (cursor < end) {
      func(cursor, array[cursor])
      cursor++
    }
  }

  actual fun reset(position: Int, length: Int): ByteBuffer {
    ensureOpen()
    this.position = position
    limit = position + length
    return this
  }

  actual fun put(value: Byte): Boolean {
    if (closed) {
      return false
    }
    if (_position >= limit) {
      return false
    }
    array[_position] = value
//    ref0(0) { array, dataSize ->
//      array[position++] = value
//    }
    _position++
    return true
  }

  actual override fun clear() {
    ensureOpen()
    limit = capacity
    position = 0
  }

  actual fun realloc(newSize: Int): ByteBuffer {
    ensureOpen()
    val new = ByteBuffer(newSize)
    if (newSize > capacity) {
      array.copyInto(
        destination = new.array,
        destinationOffset = 0,
        startIndex = 0,
        endIndex = capacity,
      )
      new.position = position
      new.limit = limit
    } else {
      array.copyInto(
        destination = new.array,
        destinationOffset = 0,
        startIndex = 0,
        endIndex = newSize
      )
      new.position = minOf(position, newSize)
      new.limit = minOf(limit, newSize)
    }
    return new
  }

  actual fun toByteArray(): ByteArray = toByteArray(remaining)

  actual fun toByteArray(limit: Int): ByteArray {
    ensureOpen()
    val size = minOf(limit, remaining)
    val r = ByteArray(size)
    val endPosition = position + size
    (position until endPosition).forEach {
      r[it - position] = array[it]
    }
    return r
  }

  actual fun write(data: ByteArray, offset: Int, length: Int): Int {
    ensureOpen()
    if (offset + length > data.size) throw IndexOutOfBoundsException()
    val l = minOf(remaining, length)
    (offset until (offset + l)).forEach {
      array[position++] = data[it]
    }
    return l
  }

  actual override fun compact() {
    ensureOpen()
    if (remaining > 0) {
      val size = remaining
      array.copyInto(
        destination = array,
        destinationOffset = 0,
        startIndex = position,
        endIndex = position + size
      )
      position = size
      limit = capacity
    } else {
      clear()
    }
  }

  actual fun peek(): Byte {
    ensureOpen()
    if (_position == _limit) {
      throw NoSuchElementException()
    }
    return this[_position]
  }

  actual fun subBuffer(index: Int, length: Int): ByteBuffer {
    ensureOpen()
    val new = ByteBuffer(length)
    array.copyInto(
      destination = new.array,
      destinationOffset = 0,
      startIndex = 0,
      endIndex = length
    )
    new.position = minOf(position, length)
    new.limit = minOf(limit, length)
    return new
  }

  actual fun readInto(dest: ByteBuffer): Int {
    ensureOpen()
    val l = minOf(remaining, dest.remaining)
    if (l == 0) {
      return l
    }
    array.copyInto(
      destination = dest.array,
      destinationOffset = dest.position,
      startIndex = position,
      endIndex = position + l
    )
    dest.position += l
    position += l
    return l
  }

  actual fun readInto(dest: ByteArray, offset: Int, length: Int): Int {
    require(dest.size - offset >= length)
    ensureOpen()
    val l = minOf(remaining, length)
    array.copyInto(
      destination = dest,
      destinationOffset = offset,
      startIndex = position,
      endIndex = position + l
    )
    position += l
    return l
  }

  actual fun free() {
    ensureOpen()
    val size = remaining
    if (size > 0) {
      array.copyInto(
        destination = array, destinationOffset = 0,
        startIndex = position, endIndex = position + size
      )
      position = 0
      limit = size
    } else {
      position = 0
      limit = 0
    }
  }

  actual override fun get(): ByteBuffer {
    ensureOpen()
    return this
  }

  actual override fun reestablish(buffer: ByteBuffer) {
    require(buffer === this) { "Buffer should equals this buffer" }
    check(!closed) { "Buffer closed" }
  }

  protected actual open fun preClose() {
    // Do nothing
  }

  override fun skipAll(bufferSize: Int) {
    _position = limit
  }

  override fun skipAll(buffer: ByteBuffer) {
    _position = limit
  }

  override fun skip(bytes: Long, buffer: ByteBuffer) {
    internalSkip(bytes)
  }

  override fun skip(bytes: Long, bufferSize: Int) {
    internalSkip(bytes)
  }
}

// private operator fun <T : CPointed> CPointer<T>.plus(offset: Long) =
//    (this.toLong() + offset).toCPointer<T>()

// private operator fun <T : CPointed> CPointer<T>.plus(offset: Int) =
//    (this.toLong() + offset).toCPointer<T>()
