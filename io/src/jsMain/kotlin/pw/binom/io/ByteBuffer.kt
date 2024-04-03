package pw.binom.io

import org.khronos.webgl.*
import org.w3c.files.Blob
import pw.binom.toArrayBuffer

private fun memcpy(
  dist: NativeMem,
  distOffset: Int,
  src: NativeMem,
  srcOffset: Int = 0,
  srcLength: Int = src.size - srcOffset,
) {
//    println("memcpy #1 dist=${dist::class} src=${src::class}, distOffset=$distOffset, srcOffset=$srcOffset, srcLength=$srcLength, src.size=${src.size}, dist.size=${dist.size}")
  if (dist is NativeMem.ArrayNativeMem && src is NativeMem.ArrayNativeMem) {
//        println("memcpy #2")
    src.mem.copyInto(
      destination = dist.mem,
      destinationOffset = distOffset,
      startIndex = srcOffset,
      endIndex = srcOffset + srcLength,
    )
    return
  }

  if (dist is NativeMem.ByteNativeMem && src is NativeMem.ByteNativeMem && srcOffset == 0 && srcLength == src.size) {
    dist.mem.set(src.mem, distOffset)
    return
  }
//    println("memcpy #3 srcOffset=$srcOffset, srcLength=$srcLength")
  (srcOffset until (srcOffset + srcLength)).forEachIndexed { index, it ->
    dist[index + distOffset] = src[it]
//        println("src[$it]->dist[${index + distOffset}] = ${src[it]}")
  }

//    repeat(dist.size) { index ->
//        println("dist[$index]=${dist[index]}")
//    }
}

private fun memcpy(
  dist: ByteArray,
  distOffset: Int,
  src: NativeMem,
  srcOffset: Int = 0,
  srcLength: Int = src.size - srcOffset,
) {
  (srcOffset until (srcOffset + srcLength)).forEachIndexed { index, it ->
    dist[index + distOffset] = src[it]
  }
}

sealed interface NativeMem {
  val size: Int
  operator fun get(index: Int): Byte
  operator fun set(index: Int, value: Byte)
  fun toInt8Array(startIndex: Int = 0, endIndex: Int = size): Int8Array

  class ByteNativeMem(val mem: Int8Array) : NativeMem {
    override val size: Int
      get() = mem.length

    override fun get(index: Int): Byte = mem[index]

    override fun set(index: Int, value: Byte) {
      mem[index] = value
    }

    override fun toInt8Array(startIndex: Int, endIndex: Int): Int8Array {
      if (startIndex == 0 && mem.length == endIndex) {
        return mem
      }
      return Int8Array(buffer = mem.buffer, byteOffset = startIndex, length = endIndex - startIndex)
    }
  }

  class ArrayNativeMem(val mem: ByteArray) : NativeMem {
    override val size: Int
      get() = mem.size

    override fun get(index: Int): Byte = mem[index]

    override fun set(index: Int, value: Byte) {
      mem[index] = value
    }

    override fun toInt8Array(startIndex: Int, endIndex: Int): Int8Array =
      if (startIndex == 0 && endIndex == mem.size) {
        Int8Array(mem.unsafeCast<Array<Byte>>())
      } else {
        val arr = mem.copyOfRange(fromIndex = startIndex, toIndex = endIndex)
        Int8Array(arr.unsafeCast<Array<Byte>>())
      }
  }
}

actual open class ByteBuffer(val native: NativeMem) :
  Channel,
  Buffer,
  ByteBufferProvider {

  actual companion object {
    suspend fun fromBlob(blob: Blob) = ByteBuffer(blob.toArrayBuffer())
  }

  constructor(array: Int8Array) : this(NativeMem.ByteNativeMem(array))
  constructor(array: ArrayBuffer) : this(Int8Array(array))
  constructor(array: Uint8Array) : this(array.buffer)
  actual constructor(size: Int) : this(NativeMem.ByteNativeMem(Int8Array(size)))
  actual constructor(array: ByteArray) : this(NativeMem.ArrayNativeMem(array))

//    actual companion object {
//        actual fun alloc(size: Int): AbstractByteBuffer =
//            AbstractByteBuffer(NativeMem.ByteNativeMem(Int8Array(size)), null)
//
//        actual fun alloc(size: Int, onClose: (AbstractByteBuffer) -> Unit): AbstractByteBuffer =
//            AbstractByteBuffer(NativeMem.ByteNativeMem(Int8Array(size)), onClose)
//
//        actual fun wrap(array: ByteArray): AbstractByteBuffer =
//            AbstractByteBuffer(NativeMem.ArrayNativeMem(array), null)
//    }

  override val capacity: Int
    get() = native.size

  init {
    ByteBufferMetric.inc(this)
  }

  override var position: Int = 0
    set(value) {
      require(value >= 0) { "position should be more or equals 0" }
      require(value <= limit) { "position should be less or equals limit" }
      field = value
    }
  override var limit: Int = capacity
    set(value) {
      if (value > capacity || value < 0) throw createLimitException(value)
      field = value
      if (position > value) {
        position = value
      }
    }

  override val remaining
    get(): Int {
      return limit - position
    }

  override val elementSizeInBytes: Int
    get() = 1

  private var closed = false

//    private var native = Int8Array(capacity)

  override fun flip() {
    ensureOpen()
    limit = position
    position = 0
  }

  override fun compact() {
    ensureOpen()
    if (remaining > 0) {
      val size = remaining
      memcpy(dist = native, distOffset = 0, src = native, srcOffset = position, srcLength = size)
      position = size
      limit = capacity
    } else {
      clear()
    }
  }

  override fun clear() {
    ensureOpen()
    limit = capacity
    position = 0
  }

  actual fun realloc(newSize: Int): ByteBuffer {
    ensureOpen()
    val new = ByteBuffer(newSize)
    if (newSize > capacity) {
      memcpy(dist = new.native, distOffset = 0, src = native, srcLength = capacity)
      new.position = position
      new.limit = limit
    } else {
      memcpy(dist = new.native, distOffset = 0, src = native, srcLength = newSize)
      new.position = minOf(position, newSize)
      new.limit = minOf(limit, newSize)
    }
    return new
  }

  actual fun skip(length: Long): Long = skip(length.toInt()).toLong()

  actual fun skip(length: Int): Int {
    ensureOpen()
    if (length <= 0) {
      return 0
    }

    val pos = minOf(position + length, limit)
    val len = pos - position
    position = pos
    return len
  }

  actual operator fun get(index: Int): Byte {
    ensureOpen()
    return native[index]
  }

  actual fun put(value: Byte): Boolean {
    if (closed) {
      return false
    }
    if (position >= limit) {
      return false
    }
    native[position++] = value
    return true
  }

  actual fun readInto(dest: ByteArray, offset: Int, length: Int): Int {
    require(dest.size - offset >= length)
    ensureOpen()
    val l = minOf(remaining, length)
    val destNative = NativeMem.ArrayNativeMem(dest) // TODO убрать лишнее создание объекта
    memcpy(dist = destNative, distOffset = offset, src = native, srcOffset = position, srcLength = l)
//        memcpy(dist = dest, distOffset = 0, src = native, srcOffset = position, srcLength = l)
    position += l
    return l
  }

  fun readInt8Array(size: Int): Int8Array {
    ensureOpen()
    require(size >= 0)
    if (size == 0) {
      return Int8Array(0)
    }
    val endIndex = minOf(position + size, limit)
    val result = native.toInt8Array(startIndex = position, endIndex = endIndex)
    position = endIndex
    return result
  }

  actual fun peek(): Byte {
    ensureOpen()
    return native[position]
  }

  actual fun reset(position: Int, length: Int): ByteBuffer {
    ensureOpen()
    this.position = position
    limit = position + length
    return this
  }

  override fun write(data: ByteBuffer): Int = data.readInto(this)

  actual fun getByte(): Byte {
    ensureOpen()
    if (position == limit) {
      throw EOFException()
    }
    return native[position++]
  }

  actual operator fun set(index: Int, value: Byte) {
    ensureOpen()
    native[index] = value
  }

  fun toInt8Array(): Int8Array {
    ensureOpen()
    if (remaining == 0) {
      return Int8Array(0)
    }
    val result = native.toInt8Array(startIndex = position, endIndex = limit)
    return result
  }

  fun readToInt8Array(): Int8Array {
    ensureOpen()
    val result = toInt8Array()
    position += result.length
    return result
  }

  actual fun toByteArray(): ByteArray {
    ensureOpen()
    return toByteArray(remaining)
  }

  actual fun toByteArray(limit: Int): ByteArray {
    ensureOpen()
    val size = minOf(limit, remaining)
    val r = ByteArray(size)
    val endPosition = position + size
    (position until endPosition).forEach {
      r[it - position] = native[it]
    }
    return r
  }

  actual fun subBuffer(index: Int, length: Int): ByteBuffer {
    ensureOpen()
    val new = ByteBuffer(length)
    memcpy(dist = new.native, distOffset = 0, src = native, srcLength = length)
    new.position = minOf(position, length)
    new.limit = minOf(limit, length)
    return new
  }

  actual fun free() {
    ensureOpen()
    val size = remaining
    if (size > 0) {
      memcpy(dist = native, distOffset = 0, src = native, srcOffset = position, srcLength = size)
      position = 0
      limit = size
    } else {
      position = 0
      limit = 0
    }
  }

  override fun flush() {
    ensureOpen()
  }

  override fun close() {
    ensureOpen()
    preClose()
    ByteBufferMetric.dec(this)
    closed = true
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

  actual fun readInto(dest: ByteBuffer): Int {
    ensureOpen()
    val l = minOf(remaining, dest.remaining)
    if (l == 0) {
      return l
    }
    memcpy(dist = dest.native, distOffset = dest.position, src = native, srcOffset = position, srcLength = l)
    dest.position += l
    position += l
    return l
  }

  override fun read(dest: ByteBuffer): Int = readInto(dest)

  fun write(data: Int8Array): Int {
    ensureOpen()
    val offset = 0
    val length: Int = data.length
    if (offset + length > data.length) throw IndexOutOfBoundsException()
    val l = minOf(remaining, length)
    (offset until (offset + l)).forEach {
      native[position++] = data[it]
    }
    return l
  }

  actual fun write(data: ByteArray, offset: Int, length: Int): Int {
    ensureOpen()
    if (offset + length > data.size) throw IndexOutOfBoundsException()
    val l = minOf(remaining, length)
    (offset until (offset + l)).forEach {
      native[position++] = data[it]
    }
    return l
  }

  override fun get(): ByteBuffer {
    ensureOpen()
    return this
  }

  override fun reestablish(buffer: ByteBuffer) {
    ensureOpen()
    require(buffer === this) { "Buffer should equals this buffer" }
    check(!closed) { "Buffer closed" }
  }

  protected actual open fun preClose() {
    // Do nothing
  }

  actual open val isClosed: Boolean
    get() = closed

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
