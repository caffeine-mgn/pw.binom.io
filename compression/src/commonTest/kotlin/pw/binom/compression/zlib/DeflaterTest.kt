package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.alloc
import pw.binom.nextBytes
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeflaterTest {

//    @Test
//    fun test() {
//        val d = Deflater(9, false, false)
//        val input = ByteBuffer.alloc(100)
//        Random.nextBytes(input)
//        input.clear()
//
//        val output = ByteBuffer.alloc(200)
//        val c = d.deflate(
//                input,
//                output
//        )
//        assertEquals(0, c)
//        assertFalse(d.flush(output))
//        assertEquals(200, output.remaining)
//        d.finish()
//        assertFalse(d.flush(output),"flash1")
//        assertEquals(98, output.remaining)
//        assertFalse(d.flush(output))
//        assertEquals(98, output.remaining)
//    }
}