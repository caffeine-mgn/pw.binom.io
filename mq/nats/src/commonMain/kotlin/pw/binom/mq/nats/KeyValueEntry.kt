package pw.binom.mq.nats

import pw.binom.date.DateTime
import pw.binom.fromBytes

class KeyValueEntry(
  val data: ByteArray,
  val created: DateTime,
  val revision: Long,
) {

  fun asString() = data.decodeToString()
  fun asByte(): Byte {
    check(data.size == Byte.SIZE_BYTES)
    return data[0]
  }

  fun asShort(): Short {
    check(data.size == Short.SIZE_BYTES)
    return Short.fromBytes(data)
  }

  fun asInt(): Int {
    check(data.size == Int.SIZE_BYTES)
    return Int.fromBytes(data)
  }

  fun asLong(): Long {
    check(data.size == Long.SIZE_BYTES)
    return Long.fromBytes(data)
  }

  fun asFloat(): Float {
    check(data.size == Float.SIZE_BYTES)
    return Float.fromBits(Int.fromBytes(data))
  }

  fun asDouble(): Double {
    check(data.size == Double.SIZE_BYTES)
    return Double.fromBits(Long.fromBytes(data))
  }
}
