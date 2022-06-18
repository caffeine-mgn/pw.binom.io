package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufferTest {
    @Test
    fun wrapTest() {
        val array = ByteArray(10) { it.toByte() }
        val buf = ByteBuffer.wrap(array)
        for (i in 0 until buf.capacity) {
            assertEquals(i.toByte(), buf[i])
        }
        for (i in 0 until buf.capacity) {
            buf[i] = (i + 1).toByte()
        }
        for (i in 0 until array.size) {
            assertEquals((i + 1).toByte(), array[i])
        }
    }
}
