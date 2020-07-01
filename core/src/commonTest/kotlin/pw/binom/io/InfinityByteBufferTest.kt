package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.nextBytes
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class InfinityByteBufferTest {

    @Test
    fun test() {
        val v = InfinityByteBuffer(64)
        v.write(ByteBuffer.alloc(244))
        assertEquals(244, v.readRemaining)
        assertEquals(200, v.read(ByteBuffer.alloc(200)))
        assertEquals(44, v.readRemaining)
    }

    @Test
    fun test2() {
        val v = InfinityByteBuffer(5)
        val data = ByteBuffer.alloc(10)
        Random.nextBytes(data)
        data.clear()
        v.write(data)
        assertEquals(data.capacity, v.readRemaining)

        val out = ByteBuffer.alloc(data.capacity)
        assertEquals(data.capacity, v.read(out))
        (0 until data.capacity).forEach { index ->
            assertEquals(data[index], out[index])
        }
        out.close()
        data.close()
    }
}