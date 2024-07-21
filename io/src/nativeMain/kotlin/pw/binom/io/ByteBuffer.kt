@file:OptIn(ExperimentalForeignApi::class, ExperimentalForeignApi::class)

package pw.binom.io

import kotlinx.cinterop.*
import pw.binom.memory.Memory
import pw.binom.memory.copyInto
import kotlin.time.ExperimentalTime

sealed interface MemAccess : Closeable {
  val capacity: Int

  val pointer: CArrayPointer<ByteVar>
  fun <T> access(func: (CPointer<ByteVar>) -> T): T
  fun <T> access2(func: (COpaquePointer) -> T): T

  class NativeMemory(val ptr: COpaquePointer, override val capacity: Int) : MemAccess {
    private var closed = false

    constructor(size: Int) : this(
      ptr = Memory.alloc(size.convert()),
      capacity = size,
    )

    override val pointer: CPointer<ByteVar> = ptr.reinterpret()

    override fun <T> access(func: (CPointer<ByteVar>) -> T): T {
      if (closed) {
        throw ClosedException()
      }
      return func(ptr.reinterpret())
    }

    override fun <T> access2(func: (COpaquePointer) -> T): T {
      if (closed) {
        throw ClosedException()
      }
      return func(ptr)
    }

    override fun close() {
      if (closed) {
        return
      }
      closed = true
      Memory.free(ptr)
    }
  }

  class HeapMemory(val ptr: CArrayPointer<ByteVar>, override val capacity: Int) : MemAccess {
    constructor(size: Int) : this(ptr = nativeHeap.allocArray<ByteVar>(size), capacity = size)

    override val pointer: CPointer<ByteVar> = ptr

    override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(ptr)
    override fun <T> access2(func: (COpaquePointer) -> T): T = func(ptr)

    override fun close() {
      nativeHeap.free(ptr)
    }
  }

  class ArrayMemory(array: ByteArray) : MemAccess {
    override val capacity: Int = array.size
    private val pin = array.pin()
    override val pointer: CPointer<ByteVar> = pin.addressOf(0)
    override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(pin.addressOf(0))
    override fun <T> access2(func: (COpaquePointer) -> T): T = func(pin.addressOf(0))

    override fun close() {
      pin.unpin()
    }
  }

  object EmptyMemory : MemAccess {
    override val capacity: Int = 0
    override val pointer: CPointer<ByteVar> = 1L.toCPointer()!!
    override fun <T> access(func: (CPointer<ByteVar>) -> T): T = TODO()
    override fun <T> access2(func: (COpaquePointer) -> T): T = TODO()

    override fun close() {
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
actual open class ByteBuffer private constructor(
  /**
   * Access to native memory
   */
  val data: MemAccess,
) : Channel, Buffer, ByteBufferProvider {
  actual companion object;

  actual constructor(size: Int) : this(MemAccess.HeapMemory(size))
  actual constructor(array: ByteArray) : this(
    if (array.isEmpty()) {
      MemAccess.EmptyMemory
    } else {
      MemAccess.ArrayMemory(
        array,
      )
    },
  )

  override val capacity: Int
    get() = data.capacity
  override val hasRemaining: Boolean
    get() = remaining > 0

  override val elementSizeInBytes: Int
    get() = 1

  @PublishedApi
  internal var closed = false

  private var _position = 0 // = AtomicInt(0)
  private var _limit = data.capacity // = AtomicInt(data.capacity)

  override val remaining: Int
    get() = _limit - _position

  override var position: Int
    get() = _position
    set(value) {
      require(value in 0.._limit) { "Position should be in range between 0 and limit. limit: $_limit, new position: $value" }
      _position = value
    }

  override var limit: Int
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

  override fun <T> refTo(position: Int, func: (CPointer<ByteVar>) -> T): T? {
    ensureOpen()
    require(position >= 0) { "position ($position) should be more or equals 0" }
    if (capacity == 0 || position == capacity) {
      return null
    }
    if (position > capacity) {
      throw IllegalArgumentException("position ($position) should be less than capacity ($capacity)")
    }
    return func((data.pointer + position)!!)
  }

  inline fun <T : Any> refTo2(defaultValue: T, position: Int, func: (ptr: CPointer<ByteVar>) -> T): T {
//    ensureOpen()
    if (closed) {
      return defaultValue
    }
    if (position < 0) {
      return defaultValue
    }
//    require(position >= 0) { "position ($position) should be more or equals 0" }
    if (capacity == 0 || position == capacity) {
      return defaultValue
    }
    if (position > capacity) {
      return defaultValue
//      throw IllegalArgumentException("position ($position) should be less than capacity ($capacity)")
    }
    return func((data.pointer + position)!!)
  }

  inline fun <T : Any> ref(defaultValue: T, func: (ptr: CPointer<ByteVar>, remaining: Int) -> T) =
    refTo2(defaultValue, position) {
      func(it, remaining)
    }

  inline fun <T : Any> ref0(defaultValue: T, func: (ptr: CPointer<ByteVar>, remaining: Int) -> T) =
    refTo2(defaultValue, 0) { ptr ->
      func(ptr, capacity)
    }

  override fun flip() {
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

  actual fun readInto(dest: ByteBuffer): Int {
    if (closed || dest.closed) {
      return 0
    }
    val r = run {
      val remaining2 = remaining
      val destRemaining = dest.remaining
      val len = minOf(destRemaining, remaining2)
      if (len <= 0) {
        0
      } else {
        val sourceCPointer = (data.pointer + position)!!
        val destCPointer = (dest.data.pointer + dest.position)!!
        val p = position
        val p2 = dest.position
        try {
          position += len
          dest.position += len
          sourceCPointer.copyInto(
            dest = destCPointer,
            size = len.convert(),
          )
          len
        } catch (e: Throwable) {
          position = p
          dest.position = p2
          throw e
        }
      }
    }
    return r
  }

  override fun read(dest: ByteBuffer) = DataTransferSize.ofSize(readInto(dest))

  @OptIn(ExperimentalTime::class)
  override fun write(data: ByteBuffer) = DataTransferSize.ofSize(data.readInto(this))

  override fun flush() {

  }

  override fun close() {
    if (!closed) {

      preClose()
      closed = true
      data.close()
      ByteBufferMetric.dec(this)
      ByteBufferAllocationCallback.onFree(this)
    }
  }

  actual operator fun get(index: Int): Byte {
    ensureOpen()
    return data.pointer[index]
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
      data.pointer[index] = newValue
    }
  }

  actual operator fun set(index: Int, value: Byte) {
    ensureOpen()
    ref0(0) { array, dataSize ->
      array[index] = value
    }
  }

  actual fun getByte(): Byte {
    ensureOpen()
    val p = _position
    if (p >= limit) throw EOFException()
    _position = p + 1
    return data.pointer[p]
//        return data.access { it[p] }
//        return data[p]
  }

  private inline fun internalForEachIndexed(func: (Int, Byte) -> Unit) {
    val start = _position
    val end = _limit
    var cursor = start
    while (cursor < end) {
      func(cursor, data.pointer[cursor])
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
    refTo2(0, _position) { array ->
      array[0] = value
//      array[position++] = value
    }
//    ref0(0) { array, dataSize ->
//      array[position++] = value
//    }
    _position++
    return true
  }

  override fun clear() {
    ensureOpen()
    limit = capacity
    position = 0
  }

  @OptIn(UnsafeNumber::class)
  actual fun realloc(newSize: Int): ByteBuffer {
    ensureOpen()
    if (newSize == 0) {
      return ByteBuffer(0)
    }
    if (capacity == 0) {
      return ByteBuffer(newSize).empty()
    }
    val new = ByteBuffer(newSize)
    val len = minOf(capacity, newSize)
    ref0(0) { oldCPointer, oldDataSize ->
      new.ref0(0) { newCPointer, newDataSize ->
        // performance equals `memcpy`
        oldCPointer.copyInto(
          dest = newCPointer,
          size = len.convert(),
        )
      }
    }
    new.position = minOf(position, new.capacity)
    new.limit = minOf(limit, new.capacity)
    return new
  }

  actual fun toByteArray(): ByteArray = toByteArray(remaining)
  actual fun toByteArray(limit: Int): ByteArray {
    ensureOpen()
    val size = minOf(remaining, limit)
    if (size == 0) {
      return byteArrayOf()
    }
    val r = ByteArray(size)
    if (size > 0) {
      ref(0) { ptr, remaining ->
        r.usePinned {
          ptr.copyInto(
            dest = it.addressOf(0),
            size = size.convert(),
          )
        }
      }
    }
    return r
  }

  actual fun write(data: ByteArray, offset: Int, length: Int): Int {
    if (closed) {
      return 0
    }
    /*
    require(offset >= 0) { "Offset argument should be more or equals 0. Actual value is $offset" }
    require(length >= 0) { "Length argument should be more or equals 0. Actual value is $length" }
    if (length == 0) {
      return 0
    }
    if (offset + length > data.size) {
      throw IndexOutOfBoundsException()
    }
     */
    val len = minOf(remaining, length, data.size - offset)
    if (len == 0) {
      return 0
    }

    data.usePinned { data ->
      ref0(0) { cPointer, dataSize ->
        data.addressOf(offset).copyInto(
          dest = (cPointer + _position)!!.reinterpret(),
          size = len.convert(),
        )
        _position += len
      }
    }

    return len
  }

  override fun compact() {
    ensureOpen()
    if (remaining > 0) {
      val size = remaining
      refTo2(0, _position) { cPointer ->
        cPointer.copyInto(
          dest = cPointer,
          size = size.convert(),
        )
        _position = size
        _limit = capacity
      }
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
    val newBytes = ByteBuffer(length)
    ref0(0) { oldCPointer, oldDataSize ->
      newBytes.ref0(0) { newCPointer, newDataSize ->
        (oldCPointer + index)!!.copyInto(
          dest = newCPointer,
          size = length.convert(),
        )
      }
    }

    return newBytes
  }

  actual fun readInto(dest: ByteArray, offset: Int, length: Int): Int {
//    ensureOpen()
    if (closed) {
      return 0
    }
//    if (dest.size - offset < length) {
//      return 0
//    }

    val lengthForCopy = minOf(remaining, length, dest.size - offset)
    if (lengthForCopy <= 0) {
      return 0
    }
//    require(dest.size - offset >= length) { "length more then available space" }
    return refTo2(0, _position) { cPointer ->
      dest.usePinned { dest ->
        cPointer.copyInto(
          dest = dest.addressOf(offset),
          size = lengthForCopy.convert(),
        )
      }
      _position += lengthForCopy
      lengthForCopy
    }
  }

  actual fun free() {
    ensureOpen()
    val size = remaining
    if (size > 0) {
      refTo2(0, position) { native ->
        native.copyInto(
          dest = native,
          size = size.convert(),
        )
      }
      position = 0
      limit = size
    } else {
      position = 0
      limit = 0
    }
  }

  override fun get(): ByteBuffer {
    ensureOpen()
    return this
  }

  override fun reestablish(buffer: ByteBuffer) {
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
