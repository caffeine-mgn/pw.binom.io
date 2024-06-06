package pw.binom.io

import java.nio.BufferUnderflowException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import java.nio.ByteBuffer as JByteBuffer

actual open class ByteBuffer(var native: JByteBuffer) :
  Channel,
  Buffer,
  ByteBufferProvider {
  actual companion object;

  //  actual constructor(size: Int) : this(JByteBuffer.allocateDirect(size))
  actual constructor(size: Int) : this(JByteBuffer.allocate(size))
  actual constructor(array: ByteArray) : this(JByteBuffer.wrap(array))

//    actual companion object {
//        actual fun alloc(size: Int): AbstractByteBuffer = AbstractByteBuffer(JByteBuffer.allocateDirect(size), null)
//
//        actual fun alloc(size: Int, onClose: (AbstractByteBuffer) -> Unit): AbstractByteBuffer =
//            AbstractByteBuffer(JByteBuffer.allocateDirect(size), onClose)
//
//        fun wrap(native: JByteBuffer) = AbstractByteBuffer(native, null)
//        actual fun wrap(array: ByteArray): AbstractByteBuffer = AbstractByteBuffer(JByteBuffer.wrap(array), null)
//    }

//    init {
//        val stack = Thread.currentThread().stackTrace.joinToString { "${it.className}.${it.methodName}:${it.lineNumber} ->" }
//        println("create ${rr++}   $stack")
//    }

  private var closed = false

  actual open val isClosed: Boolean
    get() = closed

  override fun flip() {
    ensureOpen()
    native.flip()
  }

  override val remaining: Int
    get() {
      return native.remaining()
    }

  actual fun skip(length: Long): Long = skip(length.toInt()).toLong()

  actual fun skip(length: Int): Int {
    ensureOpen()
    if (length <= 0) {
      return 0
    }
    val pos = minOf(native.position() + length, native.limit())
    val len = pos - native.position()
    native.position(pos)
    return len
  }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//        return data.buffer.update(offset, length) { data ->
//            native.copyTo(data)
//        }
//    }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//        return data.buffer.update(offset, length) { data ->
//            data.copyTo(native)
//        }
//    }

  override fun write(data: ByteBuffer): Int {
    ensureOpen()
    if (data === this) {
      throw IllegalArgumentException()
    }
    val l = minOf(remaining, data.remaining)
    length(l) { self ->
      data.length(l) { src ->
        self.native.put(src.native)
      }
    }
    return l
  }

  override fun flush() {
    ensureOpen()
  }

  override fun close() {
    if (closed){
      return
    }
    closed = true
    preClose()
    ByteBufferMetric.dec(this)
    native = JByteBuffer.allocate(0)
  }

  override var position: Int
    get() {
      return native.position()
    }
    set(value) {
      native.position(value)
    }
  override var limit: Int
    get() {
      return native.limit()
    }
    set(value) {
      native.limit(value)
    }

  override val capacity: Int
    get() {
      return native.capacity()
    }

  init {
    ByteBufferMetric.inc(this)
  }

  actual operator fun get(index: Int): Byte {
    ensureOpen()
    return native.get(index)
  }

  actual operator fun set(index: Int, value: Byte) {
    ensureOpen()
    native.put(index, value)
  }

  actual fun readInto(dest: ByteBuffer): Int {
    ensureOpen()
    val len = minOf(remaining, dest.remaining)
    if (len == 0) {
      return len
    }
    val selfLimit = native.limit()
    val destLimit = dest.native.limit()
    native.limit(native.position() + len)
    dest.native.limit(dest.native.position() + len)
    dest.native.put(native)
    native.limit(selfLimit)
    dest.native.limit(destLimit)
    return len
  }

  override fun read(dest: ByteBuffer): Int = readInto(dest)

  actual fun readInto(dest: ByteArray, offset: Int, length: Int): Int {
    if (closed) {
      return 0
    }
    val l = minOf(remaining, length, dest.size - offset)
    native.get(dest, offset, l)
    return l
  }

  actual fun getByte(): Byte {
    ensureOpen()
    try {
      return native.get()
    } catch (e: BufferUnderflowException) {
      throw EOFException()
    }
  }

  actual fun reset(position: Int, length: Int): ByteBuffer {
    ensureOpen()
    native.position(position)
    native.limit(position + length)
    return this
  }

  actual fun put(value: Byte): Boolean {
    if (closed) {
      return false
    }
    if (position >= limit) {
      return false
    }
    native.put(value)
    return true
  }

  override fun clear() {
    ensureOpen()
    native.clear()
  }

  override val elementSizeInBytes: Int
    get() = 1

  actual fun realloc(newSize: Int): ByteBuffer {
    ensureOpen()
    val new = ByteBuffer(newSize)
    if (newSize > capacity) {
      native.hold(0, capacity) { self ->
        new.native.update(0, native.capacity()) { new ->
          new.put(self)
        }
      }
      new.position = position
      new.limit = limit
    } else {
      native.hold(0, newSize) { self ->
        new.native.update(0, newSize) { new ->
          new.put(self)
        }
      }
      new.position = minOf(position, newSize)
      new.limit = minOf(limit, newSize)
    }
    return new
  }

  actual fun toByteArray(): ByteArray {
    ensureOpen()
    return toByteArray(remaining)
  }

  actual fun toByteArray(limit: Int): ByteArray {
    ensureOpen()
    val r = ByteArray(minOf(remaining, limit))
    val p = native.position()
    val l = native.limit()
    try {
      native.get(r)
    } finally {
      native.position(p)
      native.limit(l)
    }
    return r
  }

  actual fun write(data: ByteArray, offset: Int, length: Int): Int {
    if (closed) {
      return 0
    }
    val l = minOf(remaining, length, data.size - offset)
    if (l == 0) {
      return 0
    }
    native.put(data, offset, l)
    return l
  }

  override fun compact() {
    ensureOpen()
    if (position == 0) {
      native.clear()
    } else {
      native.compact()
    }
  }

  actual fun peek(): Byte {
    ensureOpen()
    if (position == limit) {
      throw NoSuchElementException()
    }
    return get(position)
  }

  actual fun subBuffer(index: Int, length: Int): ByteBuffer {
    ensureOpen()
    val p = position
    val l = limit
    try {
      position = 0
      limit = capacity
      position = index
      limit = index + length
      val newBytes = ByteBuffer(length)
      newBytes.write(this)
      return newBytes
    } finally {
      position = p
      limit = l
    }
  }

  actual fun free() {
    ensureOpen()
    val newLimit = remaining
    native.compact()
    native.position(0)
    native.limit(newLimit)
  }

  override fun get(): ByteBuffer = this

  override fun reestablish(buffer: ByteBuffer) {
    require(buffer === this) { "Buffer should equals this buffer" }
    check(!closed) { "Buffer closed" }
  }

  protected actual open fun preClose() {
    // Do nothing
  }

  internal actual open fun ensureOpen() {
    if (closed) {
      throw ClosedException()
    }
  }

  override fun skipAll(bufferSize: Int) {
    position = limit
  }

  override fun skipAll(buffer: ByteBuffer) {
    position = limit
  }

  override fun skip(bytes: Long, buffer: ByteBuffer) {
    internalSkip(bytes)
  }

  override fun skip(bytes: Long, bufferSize: Int) {
    internalSkip(bytes)
  }
}

private inline fun JByteBuffer.copyTo(buffer: JByteBuffer): Int {
  val l = minOf(buffer.remaining(), remaining())
  buffer.put(buffer)
  return l
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> JByteBuffer.hold(offset: Int, length: Int, func: (JByteBuffer) -> T): T {
  contract {
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  val p = position()
  val l = limit()
  try {
    position(offset)
    limit(offset + length)
    return func(this)
  } finally {
    limit(l)
    position(p)
  }
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> JByteBuffer.update(offset: Int, length: Int, func: (JByteBuffer) -> T): T {
  contract {
    callsInPlace(func, InvocationKind.EXACTLY_ONCE)
  }
  try {
    position(offset)
    limit(offset + length)
    return func(this)
  } finally {
    clear()
  }
}

fun JByteBuffer.putSafeInto(buffer: JByteBuffer): Int {
  val l = limit()
  val r = buffer.remaining()
  this.limit(position() + minOf(buffer.remaining(), remaining()))
  buffer.put(this)
  limit(l)
  return r - buffer.remaining()
}
