package pw.binom.mq.nats

import pw.binom.toByteArray

interface KeyValue {
  /**
   * Name of the bucket.
   */
  val bucketName: String

  suspend operator fun get(key: String): KeyValueEntry?
  suspend operator fun set(key: String, value: ByteArray)
  suspend operator fun set(key: String, value: String) =
    set(
      key = key,
      value = value.encodeToByteArray(),
    )

  suspend operator fun set(key: String, value: Byte) =
    set(
      key = key,
      value = byteArrayOf(value),
    )

  suspend operator fun set(key: String, value: Short) =
    set(
      key = key,
      value = value.toByteArray(),
    )

  suspend operator fun set(key: String, value: Int) =
    set(
      key = key,
      value = value.toByteArray(),
    )

  suspend operator fun set(key: String, value: Long) =
    set(
      key = key,
      value = value.toByteArray(),
    )

  suspend operator fun set(key: String, value: Float) =
    set(
      key = key,
      value = value.toByteArray(),
    )

  suspend operator fun set(key: String, value: Double) =
    set(
      key = key,
      value = value.toByteArray(),
    )
}
