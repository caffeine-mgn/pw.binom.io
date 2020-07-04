package pw.binom.base64

import pw.binom.asUTF8ByteArray
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
        Base64EncodeOutput(sb).use {
            it.write(data)
        }
        assertEquals("SGVsbG8gd29ybGQh", sb.toString())
    }

    @Test
    fun test2() {
        val out = StringBuilder()
        Base64EncodeOutput(out).also {
            it.write(0)
            it.write(25)
            it.write(26)
            it.write(51)
            it.write(52)
            it.write(61)
            it.write(62)
            it.write(63)
        }
        assertEquals("ABkaMzQ9Pj",out.toString())
    }
}