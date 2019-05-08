package pw.binom

import pw.binom.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBase64DecodeAppendable {

    @Test
    fun test() {
        val out = ByteArrayOutputStream()
        val o = Base64DecodeAppendable(out)
        val txt = "Hello world!"
        o.append(Base64.encode(txt.asUTF8ByteArray()))
        assertEquals(txt, out.toByteArray().asUTF8String())
    }

    @Test
    fun test2() {
        assertEquals(0, charFromBase64('A'))
        assertEquals(25, charFromBase64('Z'))
        assertEquals(26, charFromBase64('a'))
        assertEquals(51, charFromBase64('z'))
        assertEquals(52, charFromBase64('0'))
        assertEquals(61, charFromBase64('9'))
        assertEquals(62, charFromBase64('+'))
        assertEquals(63, charFromBase64('/'))
    }
}