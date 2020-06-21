package pw.binom.compression.zlib

import pw.binom.ByteDataBuffer
import pw.binom.alloc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeflaterTest {

    @Test
    fun test() {
        val d = Deflater(9, false, false)
        val cur = Cursor()
        val input = ByteDataBuffer.alloc(100) { it.toByte() }
        val output = ByteDataBuffer.alloc(200)
        cur.outputOffset = 0
        cur.outputLength = output.size
        cur.inputOffset = 0
        cur.inputLength = input.size
        val c = d.deflate(
                cur,
                input,
                output
        )
        assertEquals(0, c)
        assertFalse(d.flush(cur, output))
        assertEquals(200, cur.availOut)
        d.finish()
        assertFalse(d.flush(cur, output),"flash1")
        assertEquals(98, cur.availOut)
        assertFalse(d.flush(cur, output))
        assertEquals(98, cur.availOut)
    }
}