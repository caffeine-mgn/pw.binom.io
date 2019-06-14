package pw.binom.io

import pw.binom.asUTF8ByteArray
import pw.binom.asUTF8String

//fun Byte.toUByteSafe() = (toInt() + 128).toUByte()
//fun UByte.toByteSafe() = (toInt() - 128).toByte()

object UTF8 {

    private inline fun _write(char: Char, ff: (Byte) -> Unit) {
        val code = char.toInt()
        when {
            code.first == 0.toByte() -> {
                ff(code.second)
            }
            code.first == 4.toByte() -> {
                if (code.second < 64) {
                    ff(-48)
                    ff((code.second.toInt() - 128).toByte())
                } else {
                    ff(-47)
                    ff((code.second.toInt() - 128 - 64).toByte())
                }
            }
        }
    }

    private inline fun _read(read: () -> Byte) =
            when (val data = read()) {
                in 0..126 -> data.toChar()
                (-48).toByte() -> {
                    ((4 shl 8) or (read().toInt() + 128)).toChar()
                }
                (-47).toByte() -> {
                    ((4 shl 8) or (read().toInt() + 128 + 64)).toChar()
                }
                else -> throw RuntimeException("Unknown Control Byte $data")
            }

    fun write(char: Char, stream: OutputStream) {
        _write(char) {
            while (!stream.write(it)) {
            }
        }
    }

    suspend fun write(char: Char, stream: AsyncOutputStream) {
        _write(char) {

            while (!stream.write(it)) {
            }
        }
    }

    fun read(stream: InputStream): Char? {

        return _read {
            return@_read stream.read()
//            do {
//                if (stream.available == 0)
//                    return null
//
//                try {
//                    return@_read stream.read()
//                } catch (e: EOFException) {
//                    //NOP
//                }
//            } while (true)
//            TODO()
        }
    }

    suspend fun read(stream: AsyncInputStream): Char? {
        return _read {
            do {
                try {
                    return@_read stream.read()
                } catch (e: EOFException) {
                    //NOP
                }
            } while (true)
            TODO()
        }
    }

    fun urlEncode(input: String): String {
        val sb = StringBuilder()
        input.asUTF8ByteArray().forEach {
            when (it) {
                '.'.toByte(),
                '-'.toByte(),
                '_'.toByte(),
                '*'.toByte(),
                in 'a'.toByte()..'z'.toByte(),
                in 'A'.toByte()..'Z'.toByte(),
                in '0'.toByte()..'9'.toByte()
                -> sb.append(it.toChar())
                else -> {
                    val bb1 = ((it.toInt() and 0xf0) shr 4)
                    val bb2 = it.toInt() and 0x0f
                    sb.append("%").append(bb1.toString(16)).append(bb2.toString(16))
                }
            }
        }
        return sb.toString()
    }

    fun urlDecode(input: String): String {
        val sb = ByteArrayOutputStream()
        var i = 0
        while (i < input.length) {
            if (input[i] == '%') {
                i++
                val b1 = (input[i].toString().toInt(16) and 0xf) shl 4
                val b2 = input[i + 1].toString().toInt(16) and 0xf
                i += 1
                sb.write((b1 + b2).toByte())
            } else {
                sb.write(input[i].toByte())
            }
            i++
        }
        return sb.toByteArray().asUTF8String()
    }
}

private val Int.first: Byte
    get() = (this ushr 8).toByte()

private val Int.second: Byte
    get() = (this and 0xff).toByte()

