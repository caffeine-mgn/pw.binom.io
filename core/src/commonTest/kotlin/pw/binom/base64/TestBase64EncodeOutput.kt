package pw.binom.base64

import pw.binom.ByteBuffer
import pw.binom.encodeBytes
import pw.binom.io.use
import pw.binom.writeByte
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBase64EncodeOutput {

    @Test
    fun test() {
        val data = "Hello world!".encodeBytes()
        val sb = StringBuilder()
        val buf = ByteBuffer.alloc(4)
        Base64EncodeOutput(sb).use { output ->
            data.forEach {
                output.writeByte(buf, it)
            }
        }
        assertEquals("SGVsbG8gd29ybGQh", sb.toString())
    }

    @Test
    fun test2() {
        val out = StringBuilder()
        val buf = ByteBuffer.alloc(4)
        Base64EncodeOutput(out).also {
            it.writeByte(buf, 0)
            it.writeByte(buf, 25)
            it.writeByte(buf, 26)
            it.writeByte(buf, 51)
            it.writeByte(buf, 52)
            it.writeByte(buf, 61)
            it.writeByte(buf, 62)
            it.writeByte(buf, 63)
        }
        assertEquals("ABkaMzQ9Pj", out.toString())
    }
}