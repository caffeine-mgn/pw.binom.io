@file:OptIn(UnsafeNumber::class)

package pw.binom

import kotlinx.cinterop.*
import pw.binom.io.Buffer
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.memory.copyInto

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class CharBuffer constructor(val bytes: ByteBuffer) : CharSequence, Closeable, Buffer {

  actual companion object {
    actual fun alloc(size: Int): CharBuffer = CharBuffer(ByteBuffer(size * Char.SIZE_BYTES))
    actual fun wrap(chars: CharArray): CharBuffer {
      val buf = ByteBuffer(chars.size * Char.SIZE_BYTES)
      chars.usePinned { pinnedChars ->
        buf.ref(0) { buf, _ ->
          pinnedChars.addressOf(0).reinterpret<ByteVar>().copyInto(
            dest = buf,
            size = (pinnedChars.get().size * Char.SIZE_BYTES).convert(),
          )
        }
      }
      return CharBuffer(buf)
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun div2(value: Int): Int {
    if (value == 0) {
      return 0
    }
    return value / 2
  }

  actual override val capacity: Int
    get() = div2(bytes.capacity)
  actual override val hasRemaining: Boolean
    get() = remaining > 0

  actual override val remaining: Int
    get() = div2(bytes.remaining)

  actual override var position: Int
    get() = div2(bytes.position)
    set(value) {
      bytes.position = value * 2
    }

  actual override var limit: Int
    get() = div2(bytes.limit)
    set(value) {
      bytes.limit = value * 2
    }
  actual override val length: Int
    get() = capacity

  actual override operator fun get(index: Int): Char {
    val b1 = bytes[index * 2]
    val b2 = bytes[index * 2 + 1]
    return Short.fromBytes(b2, b1).toInt().toChar()
  }

  actual override fun subSequence(startIndex: Int, endIndex: Int): CharBuffer {
    val newBytes = bytes.subBuffer(div2(startIndex), div2(endIndex - startIndex))
    return CharBuffer(newBytes)
  }

  actual override fun close() {
    bytes.close()
  }

  override fun <T> refTo(position: Int, func: (CPointer<ByteVar>) -> T): T? =
    bytes.refTo(position * Char.SIZE_BYTES, func)

  actual operator fun set(index: Int, value: Char) {
    val s = value.code.toShort()
    bytes[index * 2] = s[1]
    bytes[index * 2 + 1] = s[0]
  }

  actual fun peek(): Char {
    if (limit == position) {
      throw NoSuchElementException()
    }

    val b1 = bytes[position * 2]
    val b2 = bytes[position * 2 + 1]
    return Short.fromBytes(b2, b1).toInt().toChar()
  }

  actual fun get(): Char {
    val b1 = bytes.getByte()
    val b2 = bytes.getByte()
    return Short.fromBytes(b2, b1).toInt().toChar()
  }

  actual fun put(value: Char) {
    val s = value.code.toShort()
    bytes.put(s[1])
    bytes.put(s[0])
  }

  actual fun reset(position: Int, length: Int): CharBuffer {
    this.position = position
    limit = position + length
    return this
  }

  actual override fun clear() {
    position = 0
    limit = capacity
  }

  actual override val elementSizeInBytes: Int
    get() = Char.SIZE_BYTES

  actual override fun toString(): String {
    when (remaining) {
      0 -> return ""
      1 -> return get().toString()
    }
    val sb = StringBuilder()
    forEach {
      sb.append(it)
    }
    return sb.toString()
  }

  actual override fun flip() {
    bytes.flip()
  }

  actual override fun compact() {
    bytes.compact()
  }

  actual fun read(array: CharArray, offset: Int, length: Int): Int {
    if (array.isEmpty() || bytes.capacity == 0) {
      return 0
    }
    return array.usePinned { pinnedArray ->
      bytes.refTo(position * 2) { bytes ->
        val len = minOf(remaining, length)
        bytes.copyInto(
          dest = pinnedArray.addressOf(offset).reinterpret(),
          size = (len * 2).convert(),
        )
        position += len
        len
      } ?: 0
    }
  }

  actual fun realloc(newSize: Int): CharBuffer =
    CharBuffer(bytes.realloc(newSize * Char.SIZE_BYTES))

  actual fun subString(startIndex: Int, endIndex: Int): String {
    if (endIndex > capacity) {
      throw IndexOutOfBoundsException("capacity: [$capacity], startIndex: [$startIndex], endIndex: [$endIndex]")
    }
    val len = minOf(capacity, endIndex - startIndex)
    if (len == 0) {
      return ""
    }
    val array = CharArray(len)
    array.usePinned { pinnedArray ->
      bytes.refTo(startIndex * Char.SIZE_BYTES) { bytes ->
        bytes.copyInto(
          dest = pinnedArray.addressOf(0).reinterpret(),
          size = (len * Char.SIZE_BYTES).convert(),
        )
      }
    }
    return array.concatToString()
  }

  actual fun write(array: CharArray, offset: Int, length: Int): Int {
    val len = minOf(remaining, minOf(array.size - offset, length))
    val pos = position * Char.SIZE_BYTES
    if (!bytes.isReferenceAccessAvailable(pos)) {
      return 0
    }
    array.usePinned { pinnedArray ->
      bytes.refTo(pos) { bytes ->
        pinnedArray.addressOf(offset).reinterpret<ByteVar>().copyInto(
          dest = bytes,
          size = (len * Char.SIZE_BYTES).convert(),
        )
        position += len
      }
    }
    return len
  }
}
