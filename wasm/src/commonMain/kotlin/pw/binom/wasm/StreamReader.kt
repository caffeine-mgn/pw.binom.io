package pw.binom.wasm

import pw.binom.fromBytes
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import pw.binom.io.readByteArray
import pw.binom.readByte
import pw.binom.wasm.readers.toUnsignedLong
import pw.binom.wasm.visitors.ExpressionsVisitor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class StreamReader(
  private val input: Input,
  limit: UInt = UInt.MAX_VALUE,
) : WasmInput {
  var globalCursor: Int = 0
    private set
  var cursor = 0
    private set

  private val buffer = ByteBuffer(8)
  private var remaining = limit

  fun readByteArray(size: Int) = readByteArray(size, buffer)

  override fun withLimit(limit: UInt): StreamReader {
    require(remaining == UInt.MAX_VALUE || limit <= remaining)
    val reader = StreamReader(input = this, limit = limit)
    reader.globalCursor = globalCursor
    return reader
  }

  fun skip(length: Int) {
    skip(length.toLong(), buffer)
  }

  override fun read(dest: ByteBuffer): DataTransferSize {
    if (remaining == UInt.MAX_VALUE) {
      val l = input.read(dest)
      if (l.isAvailable) {
        cursor += l.length
        globalCursor += l.length
      }
      return l
    } else {
      val lim = minOf(dest.remaining, remaining.toInt())
      dest.limit = dest.position + lim
      val l = input.read(dest)
      if (l.isAvailable) {
        remaining = if (l.length.toUInt() > remaining) {
          0u
        } else {
          remaining - l.length.toUInt()
        }
        cursor += l.length
        globalCursor += l.length
      }
      return l
    }
  }

  fun readInt32() =
    (
      (readByte().toInt() and 0xff) or
        ((readByte().toInt() and 0xff) shl 8) or
        ((readByte().toInt() and 0xff) shl 16) or
        ((readByte().toInt() and 0xff) shl 24)
      )

  fun readUInt32() = readInt32().toUInt()

  fun readInt64() =
    (readByte().toLong() and 0xFFL shl 0) +
      (readByte().toLong() and 0xFFL shl 8) +
      (readByte().toLong() and 0xFFL shl 16) +
      (readByte().toLong() and 0xFFL shl 24) +
      (readByte().toLong() and 0xFFL shl 32) +
      (readByte().toLong() and 0xFFL shl 40) +
      (readByte().toLong() and 0xFFL shl 48) +
      (readByte().toLong() and 0xFFL shl 56)

  override fun i32s(): Int = readInt32()

  override fun i64s() = readInt64()

  fun readInt2(): Long {
    buffer.reset(0, 4)
    readFully(buffer)
    return Int.fromBytes(buffer[3], buffer[2], buffer[1], buffer[0]).toUnsignedLong()
  }

  override fun v33u() =
    Leb.readUnsigned(maxBits = 32) { readByte() }

  override fun v33s(firstByte: Byte): Long {
    var first = false
    return Leb.readSigned(maxBits = 32) {
      if (!first) {
        first = true
        firstByte
      } else {
        readByte()
      }
    }
  }

  override fun v32u() =
    Leb.readUnsigned(maxBits = 32) { readByte() }.toUInt()

  override fun v32s() =
    Leb.readSigned(maxBits = 32) { readByte() }.toInt()

  override fun v64u() =
    Leb.readUnsigned(maxBits = 64) { readByte() }

  override fun v64s() =
    Leb.readSigned(maxBits = 64) { readByte() }

  override fun v1u(): Boolean {
    val value = v32s()
    if (value != 1 && value != 0) {
      TODO()
    }
    return value == 1
  }

  override fun string(): String = readString()

  fun readString(): String {
    val len = v32u().toInt()
    val bytes = readByteArray(len, buffer)
    return bytes.decodeToString()
  }

  @OptIn(ExperimentalContracts::class)
  inline fun readLimit(min: (UInt) -> Unit, range: (UInt, UInt) -> Unit) {
    contract {
      callsInPlace(min, InvocationKind.AT_MOST_ONCE)
      callsInPlace(range, InvocationKind.AT_MOST_ONCE)
    }
    val limitExist = v1u()
    val min = v32u()
    if (!limitExist) {
      min(min)
      return
    }
    val max = v32u()
    range(min, max)
  }

  inline fun <T> readList(func: () -> T): List<T> {
    var count = v32u()
    val result = ArrayList<T>(count.toInt())
    while (count > 0u) {
      count--
      result += func()
    }
    return result
  }

  inline fun readVec(func: () -> Unit) {
    var count = v32u()
    while (count > 0u) {
      count--
      func()
    }
  }

  inline fun <R> readVec(init: (Int) -> R, func: (R) -> Unit) {
    var count = v32u()
    val r = init(count.toInt())
    while (count > 0u) {
      count--
      func(r)
    }
  }

  fun readByte() = readByte(buffer)
  fun readUByte() = readByte().toUByte()

  override fun sByte(): Byte = readByte(buffer)
  override fun uByte() = sByte().toUByte()

  override fun skipOther() {
    if (remaining == UInt.MAX_VALUE || remaining == 0u) {
      return
    }
    println("-------skiping $remaining bytes-------")
    val r = remaining
    try {
      skip(remaining.toLong(), buffer)
    } catch (e: Throwable) {
      throw IllegalStateException("Can't skip $r bytes", e)
    }
  }

  override fun close() {
    if (remaining != UInt.MAX_VALUE && remaining > 0u) {
      throw IllegalStateException("Not all data was read. remaining=$remaining")
    }
    buffer.close()
  }
}
