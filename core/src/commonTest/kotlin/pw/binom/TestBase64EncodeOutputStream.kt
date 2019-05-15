package pw.binom

import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

fun Byte.toBinary(max: Int = 6): String {
    val ss = this.toString(2)
    val sb = StringBuilder()
    while (sb.length + ss.length < max) {
        sb.append("0")
    }
    sb.append(ss)
    return sb.toString()
}

class TestBase64EncodeOutputStream {

    @Test
    fun test() {
        val data = "Hello world!".asUTF8ByteArray()
        data.forEachIndexed { index, byte ->
            println("$index->${byte.toBinary()}")
        }
        val sb = StringBuilder()
        Base64EncodeOutputStream(sb).use {
            it.write(data)
        }
        assertEquals("SGVsbG8gd29ybGQh", sb.toString())
    }

    @Test
    fun test2() {
        assertEquals('A', byteToBase64(0))
        assertEquals('Z', byteToBase64(25))
        assertEquals('a', byteToBase64(26))
        assertEquals('z', byteToBase64(51))
        assertEquals('0', byteToBase64(52))
        assertEquals('9', byteToBase64(61))
        assertEquals('+', byteToBase64(62))
        assertEquals('/', byteToBase64(63))
    }
}