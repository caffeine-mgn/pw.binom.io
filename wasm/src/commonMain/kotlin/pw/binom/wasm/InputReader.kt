package pw.binom.wasm

import pw.binom.fromBytes
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import pw.binom.io.readByteArray
import pw.binom.readByte
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class InputReader(
  private val input: Input,
  limit: Int = Int.MAX_VALUE,
) : Input {
  var globalCursor: Int = 0
    private set
  var cursor = 0
    private set

  init {
    require(limit > 0) { "limit should be equals or greater than 0" }
  }

  private val buffer = ByteBuffer(8)
  private var remaining = limit

  fun readByteArray(size: Int) = readByteArray(size, buffer)

  fun withLimit(limit: Int): InputReader {
    require(remaining == Int.MAX_VALUE || limit <= remaining)
    val reader = InputReader(input = this, limit = limit)
    reader.globalCursor = globalCursor
    return reader
  }

  fun skip(length: Int) {
    skip(length.toLong(), buffer)
  }

  override fun read(dest: ByteBuffer): DataTransferSize {
    if (remaining == Int.MAX_VALUE) {
      val l = input.read(dest)
      if (l.isAvailable) {
        cursor += l.length
        globalCursor += l.length
      }
      return l
    } else {
      val lim = minOf(dest.remaining, remaining)
      dest.limit = dest.position + lim
      val l = input.read(dest)
      if (l.isAvailable) {
        remaining -= l.length
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

  fun readUInt64() = readInt64().toULong()

  fun readInt2(): Long {
    buffer.reset(0, 4)
    readFully(buffer)
    return Int.fromBytes(buffer[3], buffer[2], buffer[1], buffer[0]).toUnsignedLong()
  }

  fun readVarUInt33() = readUnsignedLeb128L((33 + 6) / 7)
  fun readVarUInt32L() = readUnsignedLeb128L((32 + 6) / 7)
  fun readVarUInt1() = readUnsignedLeb128().let {
    if (it != 1 && it != 0) throw RuntimeException("IoErr.InvalidLeb128Number()")
    it == 1
  }

  fun readVarInt32() = readSignedLeb128().let {
    if (it < Int.MIN_VALUE.toLong() || it > Int.MAX_VALUE.toLong()) throw RuntimeException("IoErr.InvalidLeb128Number()")
    it.toInt()
  }

  fun readVarUInt32() = readUnsignedLeb128().toUnsignedLong()
  fun readVarUInt32AsInt() = readVarUInt32().toIntExact()

  fun readString(): String {
    val len = this.readVarUInt32AsInt()
    val bytes = readByteArray(len, buffer)
    return bytes.decodeToString()
  }

  fun readBlockType() {
    val firstByte1 = readVarUInt33().toULong()
    val firstByte = firstByte1.toUByte()
    val type = if (firstByte == 0x40u.toUByte()) {
      null
    } else {
      if (isValueType(firstByte)) {
        readValueType(firstByte)
      } else {
        val index = readLebSigned()
        println("block type index: $index")
//        readUnsignedLeb128(4)
      }
    }
  }

  @OptIn(ExperimentalContracts::class)
  inline fun readLimit(min: (UInt) -> Unit, range: (UInt, UInt) -> Unit) {
    contract {
      callsInPlace(min, InvocationKind.AT_MOST_ONCE)
      callsInPlace(range, InvocationKind.AT_MOST_ONCE)
    }
    val limitExist = readVarUInt1()
    val min = readVarUInt32L().toUInt()
    if (!limitExist) {
      min(min)
      return
    }
    val max = readVarUInt32L().toUInt()
    range(min, max)
  }

  inline fun <T> readList(func: () -> T): List<T> {
    var count = readVarUInt32()
    val result = ArrayList<T>(count.toInt())
    while (count > 0) {
      count--
      result += func()
    }
    return result
  }

  inline fun readVec(func: () -> Unit) {
    var count = readVarUInt32L()
    while (count > 0) {
      count--
      func()
    }
  }

  inline fun <R> readVec(init: (Int) -> R, func: (R) -> Unit) {
    var count = readVarUInt32()
    val r = init(count.toInt())
    while (count > 0) {
      count--
      func(r)
    }
  }

  fun readSignedLeb128(maxCount: Int = 4): Long {
    // Taken from Android source, Apache licensed
    var result = 0L
    var cur: Int
    var count = 0
    var signBits = -1L
    do {
      cur = readByte(buffer).toInt() and 0xff
      result = result or ((cur and 0x7f).toLong() shl (count * 7))
      signBits = signBits shl 7
      count++
    } while (cur and 0x80 == 0x80 && count <= maxCount)
    if (cur and 0x80 == 0x80) throw RuntimeException("IoErr.InvalidLeb128Number()")

    // Check for 64 bit invalid, taken from Apache/MIT licensed:
    //  https://github.com/paritytech/parity-wasm/blob/2650fc14c458c6a252c9dc43dd8e0b14b6d264ff/src/elements/primitives.rs#L351
    // TODO: probably need 32 bit checks too, but meh, not in the suite
    if (count > maxCount && maxCount == 9) {
      if (cur and 0b0100_0000 == 0b0100_0000) {
        if ((cur or 0b1000_0000).toByte() != (-1).toByte()) throw RuntimeException("IoErr.InvalidLeb128Number()")
      } else if (cur != 0) {
        throw RuntimeException("IoErr.InvalidLeb128Number()")
      }
    }

    if ((signBits shr 1) and result != 0L) result = result or signBits
    return result
  }

  fun readVarInt7() = readSignedLeb128().let {
    if (it < Byte.MIN_VALUE.toLong() || it > Byte.MAX_VALUE.toLong()) throw RuntimeException("IoErr.InvalidLeb128Number($it)")
    it.toByte()
  }

  fun readByte() = readByte(buffer)
  fun readUByte() = readByte().toUByte()

  fun readVarUInt7() = readUnsignedLeb128().let {
    if (it > 255) throw RuntimeException("IoErr.InvalidLeb128Number()")
    it.toShort()
  }

  fun readUnsignedLeb128L(maxCount: Int = 4): Long {
    // Taken from Android source, Apache licensed
    var result = 0L
    var cur: Long
    var count = 0
    do {
      val byte = readByte()
      cur = byte.toLong() and 0xffL
      result = result or ((cur and 0x7fL) shl (count * 7))
      count++
    } while (cur and 0x80L == 0x80L && count <= maxCount)
    if (cur and 0x80L == 0x80L)
      throw RuntimeException("IoErr.InvalidLeb128Number()")
    // Result can't have used more than ceil(result/7)
    if (cur != 0L && count - 1 > (result + 6) / 7) throw RuntimeException("IoErr.InvalidLeb128Number()")
    return result
  }

  fun readUnsignedLeb128(maxCount: Int = 4): Int {
    // Taken from Android source, Apache licensed
    var result = 0
    var cur: Int
    var count = 0
    do {
      cur = readByte().toInt() and 0xff
      result = result or ((cur and 0x7f) shl (count * 7))
      count++
    } while (cur and 0x80 == 0x80 && count <= maxCount)
    if (cur and 0x80 == 0x80)
      throw RuntimeException("IoErr.InvalidLeb128Number()")
    // Result can't have used more than ceil(result/7)
    if (cur != 0 && count - 1 > (result.toUnsignedLong() + 6) / 7) throw RuntimeException("IoErr.InvalidLeb128Number()")
    return result
  }

  fun skipOther() {
    if (remaining == Int.MAX_VALUE || remaining == 0) {
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
    if (remaining != Int.MAX_VALUE && remaining > 0) {
      throw IllegalStateException("Not all data was read. remaining=$remaining")
    }
    buffer.close()
  }

  fun readLebSigned() = Leb.readSigned { readByte() }
  fun readLebUnsigned() = Leb.readUnsigned { readByte() }
}
