package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer
import kotlin.test.Test

class CharsetTest {

    @Test
    fun test() {
        val buf = CharBuffer.alloc(30)
        Charsets.get("WINDOWS-1251").newDecoder()
            .decode(ByteBuffer(test_data_hello_bytes_windows_1251), buf)
        buf.flip()
        println("data: ${buf.remaining}  $buf ${buf.toString().length}")
        buf.toString().forEachIndexed { index, c ->
            println("$index -> $c (${c.code})")
        }
        "Привет".forEachIndexed { index, c ->
            println("$index -> $c (${c.code})")
        }
    }
}
