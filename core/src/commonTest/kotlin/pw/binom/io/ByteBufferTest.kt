package pw.binom.io

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufferTest {

    @Test
    fun test() {
        val v = ByteBuffer(64)
        v.write(ByteArray(244))
        assertEquals(244, v.readRemaining)
        assertEquals(200, v.read(ByteArray(200)))
        assertEquals(44, v.readRemaining)
    }

    @Test
    fun test2() {
        val v = ByteBuffer(5)
        val data = ByteArray(10)
        Random.nextBytes(data)
        v.write(data)

        val out = ByteArray(data.size)
        assertEquals(data.size, v.read(out))
        data.forEachIndexed { index, byte ->
            println("Test $index")
            assertEquals(byte, out[index])
        }
    }
}