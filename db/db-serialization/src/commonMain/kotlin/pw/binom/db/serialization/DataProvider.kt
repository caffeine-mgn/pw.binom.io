package pw.binom.db.serialization

import kotlinx.serialization.SerializationException
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.date.parseIso8601Date
import pw.binom.uuid.UUID

interface DataProvider {
  fun getString(key: String): String {
    val value = get(key) ?: throw NullPointerException("Value \"$key\" is null")
    return value.toString()
  }

  fun getBoolean(key: String): Boolean = getString(key).let { it == "t" || it == "true" }

  fun isNull(key: String): Boolean = get(key) == null

  fun getInt(key: String): Int = getString(key).toInt()

  fun getLong(key: String): Long = getString(key).toLong()

  fun getFloat(key: String): Float = getString(key).toFloat()

  fun getDouble(key: String): Double = getString(key).toDouble()

  fun getShort(key: String): Short = getString(key).toShort()

  fun getByte(key: String): Byte = getString(key).toByte()

  fun getByteArray(key: String): ByteArray = getString(key).encodeToByteArray()

  fun getChar(key: String): Char = getString(key).let { it[0] }

  fun getUUID(key: String): UUID = UUID.fromString(getString(key))

  fun getDateTime(key: String): DateTime {
    val str = getString(key)
    return str.parseIso8601Date() ?: throw SerializationException("Can't parse $str to DateTime")
  }

  fun getDate(key: String): Date {
    val str = getString(key)
    return Date.fromIso8601(str)
  }

  operator fun get(key: String): Any?

  operator fun contains(key: String): Boolean

  companion object {
    val EMPTY =
      object : DataProvider {
        private fun throwException(): Nothing = throw IllegalStateException("Not supported")

        override fun getString(key: String) = throwException()

        override fun getBoolean(key: String) = throwException()

        override fun isNull(key: String) = throwException()

        override fun getInt(key: String) = throwException()

        override fun getLong(key: String) = throwException()

        override fun getFloat(key: String) = throwException()

        override fun getDouble(key: String) = throwException()

        override fun getShort(key: String) = throwException()

        override fun getByte(key: String) = throwException()

        override fun getChar(key: String) = throwException()

        override fun getUUID(key: String) = throwException()

        override fun get(key: String) = throwException()

        override fun contains(key: String) = throwException()
      }
  }
}
