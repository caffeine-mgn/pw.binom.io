package pw.binom.base64

import kotlin.test.Test
import kotlin.test.assertEquals
/*

class TestBase64DecodeAppendable {

    @Test
    fun test() {
        val out = ByteArrayOutputStream()
        val o = Base64DecodeAppendable(out)
        val txt = "Hello world!"
        println("-->${Base64.encode(txt.asUTF8ByteArray())}")
        o.append(Base64.encode(txt.asUTF8ByteArray()))
        assertEquals(txt, out.toByteArray().asUTF8String())
    }

    @Test
    fun test2() {
        val out = ByteArrayOutputStream()
        Base64DecodeAppendable(out).apply {
            append('A')
            append('Z')
            append('a')
            append('z')
            append('0')
            append('9')
            append('+')
            append('/')
        }
        out.toByteArray().also {
            assertEquals(1, it[0])
            assertEquals(-106, it[1])
            assertEquals(-77, it[2])
            assertEquals(-45, it[3])
            assertEquals(-33, it[4])
            assertEquals(-65, it[5])
        }
    }
}*/
