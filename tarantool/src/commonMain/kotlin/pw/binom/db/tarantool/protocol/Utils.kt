package pw.binom.db.tarantool.protocol

import pw.binom.*
import pw.binom.base64.Base64
import pw.binom.io.ByteArrayOutput
import pw.binom.io.IOException
import pw.binom.io.Sha1
import kotlin.experimental.xor

internal object Utils {

    fun buildAuthPacketData(
            username: String,
            password: String,
            salt: String
    ): Map<Any, Any?> {
        val sha1 = Sha1()

        val auth = ArrayList<Any>(2)
        auth.add("chap-sha1");
        sha1.update(password.encodeToByteArray())
        val p = sha1.finish()

        sha1.init()
        sha1.update(p)
        val p2 = sha1.finish()

        sha1.init()

        sha1.update(Base64.decode(salt), 0, 20)
        sha1.update(p2)
        val scramble = sha1.finish()
        for (i in 0 until 20) {
            p[i] = p[i] xor scramble[i]
        }

        auth.add(p)
        return mapOf(Key.USER_NAME.id to username, Key.TUPLE.id to auth)
    }

    private fun write(value: Long, buffer: ByteBuffer, out: Output) {
        if (value >= 0) {
            when {
                value <= MAX_7BIT -> {
                    out.writeByte(buffer, (value.toInt() or MP_FIXNUM.toInt()).toByte())
                }
                value <= MAX_8BIT -> {
                    out.writeByte(buffer, MP_UINT8)
                    out.writeByte(buffer, value.toByte())
                }
                value <= MAX_16BIT -> {
                    out.writeByte(buffer, MP_UINT16);
                    out.writeShort(buffer, value.toShort());
                }
                value <= MAX_32BIT -> {
                    out.writeByte(buffer, MP_UINT32)
                    out.writeInt(buffer, value.toInt())
                }
                else -> {
                    out.writeByte(buffer, MP_UINT64)
                    out.writeLong(buffer, value)
                }
            }
        } else {
            when {
                value >= -(MAX_5BIT + 1) -> {
                    out.writeByte(buffer, (value.toInt() and 0xff).toByte())
                }
                value >= -(MAX_7BIT + 1) -> {
                    out.writeByte(buffer, MP_INT8);
                    out.writeByte(buffer, value.toByte());
                }
                value >= -(MAX_15BIT + 1) -> {
                    out.writeByte(buffer, MP_INT16)
                    out.writeShort(buffer, value.toShort())
                }
                value >= -(MAX_31BIT + 1) -> {
                    out.writeByte(buffer, MP_INT32)
                    out.writeInt(buffer, value.toInt())
                }
                else -> {
                    out.writeByte(buffer, MP_INT64)
                    out.writeLong(buffer, value)
                }
            }
        }
    }

    private fun write(value: String, buffer: ByteBuffer, out: Output) {
        val data = value.toByteBufferUTF8()
        when {
            data.capacity <= MAX_5BIT -> {
                out.writeByte(buffer, (data.capacity or (MP_FIXSTR.toInt() and 0xFF)).toByte())
            }
            data.capacity <= MAX_8BIT -> {
                out.writeByte(buffer, MP_STR8)
                out.writeByte(buffer, data.capacity.toByte())
            }
            data.capacity <= MAX_16BIT -> {
                out.writeByte(buffer, MP_STR16);
                out.writeShort(buffer, data.capacity.toShort())
            }
            else -> {
                out.writeByte(buffer, MP_STR32);
                out.writeInt(buffer, data.capacity)
            }
        }
        out.write(data);
    }

    private fun write(value: ByteArray, buffer: ByteBuffer, out: Output) {
        val buf = ByteBuffer.wrap(value)
        try {
            write(buf, buffer, out)
        } finally {
            buf.close()
        }
    }

    private fun write(value: ByteBuffer, buffer: ByteBuffer, out: Output) {
        when {
            value.remaining <= MAX_8BIT -> {
                out.writeByte(buffer, MP_BIN8)
                out.writeByte(buffer, value.capacity.toByte())
            }
            value.remaining <= MAX_16BIT -> {
                out.writeByte(buffer, MP_BIN16)
                out.writeShort(buffer, value.capacity.toShort())
            }
            else -> {
                out.writeByte(buffer, MP_BIN32)
                out.writeInt(buffer, value.capacity)
            }
        }
        out.write(value)
    }

    private fun writeValue(value: List<Any?>, buffer: ByteBuffer, out: Output) {
        when {
            value.size <= MAX_4BIT -> {
                out.writeByte(buffer, (value.size or (MP_FIXARRAY.toInt() and 0xFF)).toByte());
            }
            value.size <= MAX_16BIT -> {
                out.writeByte(buffer, MP_ARRAY16);
                out.writeShort(buffer, value.size.toShort())
            }
            else -> {
                out.writeByte(buffer, MP_ARRAY32);
                out.writeInt(buffer, value.size)
            }
        }
        value.forEach {
            writeValue(it, buffer, out)
        }
    }

    private fun writeValue(value: Any?, buffer: ByteBuffer, out: Output) {
        when (value) {
            null -> out.writeByte(buffer, MP_NULL)
            true -> out.writeByte(buffer, MP_TRUE)
            false -> out.writeByte(buffer, MP_FALSE)
            is String -> write(value, buffer, out)
            is ByteBuffer -> write(value, buffer, out)
            is ByteArray -> write(value, buffer, out)
            is Pair<*, *> -> writeValue(listOf(value.first, value.second), buffer, out)
            is List<*> -> writeValue(value, buffer, out)
            is Long -> write(value, buffer, out)
            is Int -> write(value.toLong(), buffer, out)
            is Short -> write(value.toLong(), buffer, out)
            is Byte -> write(value.toLong(), buffer, out)
            is Map<*, *> -> write(value as Map<Any?, Any?>, buffer, out)
            is Float -> {
                out.writeByte(buffer, MP_FLOAT)
                out.writeFloat(buffer, value)
            }
            is Double -> {
                out.writeByte(buffer, MP_FLOAT)
                out.writeDouble(buffer, value)
            }
            else -> throw IllegalArgumentException("Unsupported Type ${value::class}")
        }
    }

    private fun write(map: Map<Any?, Any?>, buffer: ByteBuffer, out: Output) {
        when {
            map.size <= MAX_4BIT -> out.writeByte(buffer, (map.size or MP_FIXMAP.toInt()).toByte())
            map.size <= MAX_16BIT -> {
                out.writeByte(buffer, MP_MAP16)
                out.writeShort(buffer, map.size.toShort())
            }
            else -> {
                out.writeByte(buffer, MP_MAP32)
                out.writeInt(buffer, map.size)
            }
        }
        map.forEach { (k, v) ->
            writeValue(k, buffer, out)
            writeValue(v, buffer, out)
        }
    }

    fun makeMessage(header: Map<Int, Any?>, data: Map<Any, Any?>, out: ByteArrayOutput) {
        val buf = ByteBuffer.alloc(8)
        try {
            out.writeByte(buf, (0xce).toByte())
            out.writeInt(buf, 0)
            writeValue(header, buf, out)
            writeValue(data, buf, out)
            val l = out.size
            out.data.position = 1
            out.data.writeInt(buf, l - 5)
            out.data.position = 0
            out.data.limit = l
        } finally {
            buf.close()
        }
    }

    private suspend fun unpackListAsync(size: Int, buf: ByteBuffer, input: AsyncInput): List<Any?> {
        if (size < 0) {
            throw IllegalArgumentException("List to unpack too large for Kotlin (more than 2^31 elements)!");
        }
        if (size == 0) {
            return emptyList()
        }
        val out = ArrayList<Any?>(size)
        repeat(size) {
            out += unpackAsync(buf, input)
        }
        return out
    }

    private suspend fun unpackMapAsync(size: Int, buf: ByteBuffer, input: AsyncInput): Map<Any?, Any?> {
        if (size < 0) {
            throw IllegalArgumentException("Map to unpack too large for Kotlin (more than 2^31 elements)!");
        }
        if (size == 0) {
            return emptyMap()
        }
        val out = HashMap<Any?, Any?>()
        repeat(size) {
            val key = unpackAsync(buf, input)
            val value = unpackAsync(buf, input)
            out[key] = value
        }
        return out
    }

    suspend fun unpackStringAsync(size: Int, buf: ByteBuffer, input: AsyncInput): String =
            unpackBinAsync(size, buf, input).decodeToString()

    suspend fun unpackBinAsync(size: Int, buf: ByteBuffer, input: AsyncInput): ByteArray {
        if (size < 0) {
            throw IllegalArgumentException("ByteArray to unpack too large for Kotlin (more than 2^31 elements)!");
        }
        if (size == 0) {
            return byteArrayOf()
        }
        val cap = buf.capacity
        val out = ByteArray(size)
        var cur = 0
        while (cur < size) {
            buf.position = 0
            buf.limit = minOf(cap, size - cur)
            val l = input.read(buf)
            buf.flip()
            repeat(l) {
                out[cur++] = buf.get()
            }
        }
        return out
    }


    suspend fun unpackAsync(buf: ByteBuffer, input: AsyncInput): Any? {
        val type = input.readByte(buf)
        return when (type) {
            MP_NULL -> null
            MP_FALSE -> false
            MP_TRUE -> true
            MP_FLOAT -> input.readFloat(buf)
            MP_DOUBLE -> input.readDouble(buf)
            MP_UINT8 -> input.readByte(buf)
            MP_UINT16 -> input.readShort(buf)
            MP_UINT32 -> input.readInt(buf)
            MP_UINT64 -> input.readLong(buf)
            MP_INT8 -> input.readByte(buf)
            MP_INT16 -> input.readShort(buf)
            MP_INT32 -> input.readInt(buf)
            MP_INT64 -> input.readLong(buf)
            MP_ARRAY16 -> unpackListAsync(
                    size = input.readShort(buf).toInt() and MAX_16BIT,
                    buf = buf,
                    input = input
            )
            MP_ARRAY32 -> unpackListAsync(
                    size = input.readInt(buf),
                    buf = buf,
                    input = input
            )
//            0x83.toByte() -> unpackMapAsync(
//                    size = 3,
//                    buf = buf,
//                    input = input
//            )
            MP_MAP16 -> unpackMapAsync(
                    size = input.readShort(buf).toInt() and MAX_16BIT,
                    buf = buf,
                    input = input
            )
            MP_MAP32 -> unpackMapAsync(
                    size = input.readInt(buf),
                    buf = buf,
                    input = input
            )
            MP_STR8 -> unpackStringAsync(
                    size = input.readByte(buf).toInt() and MAX_8BIT,
                    buf = buf,
                    input = input
            )
            MP_STR16 -> unpackStringAsync(
                    size = input.readShort(buf).toInt() and MAX_16BIT,
                    buf = buf,
                    input = input
            )
            MP_STR32 -> unpackStringAsync(
                    size = input.readInt(buf),
                    buf = buf,
                    input = input
            )
            MP_BIN8 -> unpackBinAsync(
                    size = input.readByte(buf).toInt() and MAX_8BIT,
                    buf = buf,
                    input = input
            )
            MP_BIN16 -> unpackBinAsync(
                    size = input.readShort(buf).toInt() and MAX_16BIT,
                    buf = buf,
                    input = input
            )
            MP_BIN32 -> unpackBinAsync(
                    size = input.readInt(buf),
                    buf = buf,
                    input = input
            )
            else -> {
                val typeInt = (type.toInt() and 0xFF)
                return if (typeInt >= MP_NEGATIVE_FIXNUM_INT && typeInt <= MP_NEGATIVE_FIXNUM_INT + MAX_5BIT) {
                    typeInt
                } else if (typeInt >= MP_FIXARRAY_INT && typeInt <= MP_FIXARRAY_INT + MAX_4BIT) {
                    unpackListAsync(typeInt - MP_FIXARRAY_INT, buf, input)
                } else if (typeInt.toUInt() >= MP_FIXMAP_INT.toUInt() && typeInt.toUInt() <= MP_FIXMAP_INT.toUInt() + MAX_4BIT.toUInt()) {
                    unpackMapAsync(typeInt - MP_FIXMAP_INT, buf, input)
                } else if (typeInt >= MP_FIXSTR_INT && typeInt <= MP_FIXSTR_INT + MAX_5BIT) {
                    unpackStringAsync(typeInt - MP_FIXSTR_INT, buf, input)
                } else if (typeInt <= MAX_7BIT) {
                    // MP_FIXNUM - the value is value as an int
                    typeInt
                } else
                    throw IOException("Unknown data type: 0x${type.toUByte().toString(16)}")
            }
        }
    }
}

private const val MAX_4BIT = 0xf
private const val MAX_5BIT = 0x1f
private const val MAX_7BIT = 0x7f
private const val MAX_8BIT = 0xff
private const val MAX_15BIT = 0x7fff
private const val MAX_16BIT = 0xffff
private const val MAX_31BIT = 0x7fffffff
private const val MAX_32BIT = 0xffffffffL

//val BI_MIN_LONG: BigInteger = BigInteger.valueOf(Long.MIN_VALUE)
//val BI_MAX_LONG: BigInteger = BigInteger.valueOf(Long.MAX_VALUE)
//val BI_MAX_64BIT: BigInteger = BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE)

//these values are from http://wiki.msgpack.org/display/MSGPACK/Format+specification
private const val MP_NULL = 0xc0.toByte()
private const val MP_FALSE = 0xc2.toByte()
private const val MP_TRUE = 0xc3.toByte()
private const val MP_BIN8 = 0xc4.toByte()
private const val MP_BIN16 = 0xc5.toByte()
private const val MP_BIN32 = 0xc6.toByte()

private const val MP_FLOAT = 0xca.toByte()
private const val MP_DOUBLE = 0xcb.toByte()

private const val MP_FIXNUM = 0x00.toByte() //last 7 bits is value

private const val MP_UINT8 = 0xcc.toByte()
private const val MP_UINT16 = 0xcd.toByte()
private const val MP_UINT32 = 0xce.toByte()
private const val MP_UINT64 = 0xcf.toByte()

private const val MP_NEGATIVE_FIXNUM = 0xe0.toByte() //last 5 bits is value

private const val MP_NEGATIVE_FIXNUM_INT = 0xe0 //  /me wishes for signed numbers.
private const val MP_INT8 = 0xd0.toByte()
private const val MP_INT16 = 0xd1.toByte()
private const val MP_INT32 = 0xd2.toByte()
private const val MP_INT64 = 0xd3.toByte()

private const val MP_FIXARRAY = 0x90.toByte() //last 4 bits is size

private const val MP_FIXARRAY_INT = 0x90
private const val MP_ARRAY16 = 0xdc.toByte()
private const val MP_ARRAY32 = 0xdd.toByte()

private const val MP_FIXMAP = 0x80.toByte() //last 4 bits is size

private const val MP_FIXMAP_INT = 0x80
private const val MP_MAP16 = 0xde.toByte()
private const val MP_MAP32 = 0xdf.toByte()

private const val MP_FIXSTR = 0xa0.toByte() //last 5 bits is size

private const val MP_FIXSTR_INT = 0xa0
private const val MP_STR8 = 0xd9.toByte()
private const val MP_STR16 = 0xda.toByte()
private const val MP_STR32 = 0xdb.toByte()