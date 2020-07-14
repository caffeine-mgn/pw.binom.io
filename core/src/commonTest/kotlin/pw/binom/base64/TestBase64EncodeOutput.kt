package pw.binom.base64

import pw.binom.asUTF8ByteArray
import pw.binom.io.use
import pw.binom.writeByte
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBase64EncodeOutput {

    @Test
    fun test() {
        val data = "Hello world!".asUTF8ByteArray()
        val sb = StringBuilder()
        Base64EncodeOutput(sb).use { output ->
            data.forEach {
                output.writeByte(it)
            }
        }
        assertEquals("SGVsbG8gd29ybGQh", sb.toString())
    }

    @Test
    fun test2() {
        val out = StringBuilder()
        Base64EncodeOutput(out).also {
            it.writeByte(0)
            it.writeByte(25)
            it.writeByte(26)
            it.writeByte(51)
            it.writeByte(52)
            it.writeByte(61)
            it.writeByte(62)
            it.writeByte(63)
        }
        assertEquals("ABkaMzQ9Pj", out.toString())
    }
}