package pw.binom.io

import pw.binom.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteArrayOutputTest {

    @Test
    fun reallocBigPartTest() {
        val out = ByteArrayOutput(
                capacity = 512,
                capacityFactor = 1.7f
        )

        val pp = ByteBuffer.alloc(1024)
        Random.nextBytes(pp)
        pp.clear()
        out.write(pp)

        assertEquals(1024, out.data.capacity)
        assertEquals(0, out.data.remaining)
        out.data.clear()
        out.data.forEachIndexed { i, byte ->
            assertEquals(pp[i], byte)
        }
        out.trimToSize()
        out.data.clear()
        out.data.forEachIndexed { i, byte ->
            assertEquals(pp[i], byte)
        }
    }

    @Test
    fun reallocShortPartTest() {
        val out = ByteArrayOutput(
                capacity = 10,
                capacityFactor = 1.7f
        )
        val tmp = ByteBuffer.alloc(1)
        val pp = ByteBuffer.alloc(50)
        repeat(pp.capacity){
            pp.put(it.toByte())
        }
        pp.clear()
        pp.forEach {
            out.writeByte(tmp, it)
        }


//        assertEquals(1024, out.data.capacity)
//        assertEquals(0, out.data.remaining)
        assertEquals(pp.capacity, out.size)
        out.data.flip()
        out.data.forEachIndexed { i, byte ->
            assertEquals(pp[i], byte)
        }
        out.trimToSize()
        out.data.clear()
        out.data.forEachIndexed { i, byte ->
            assertEquals(pp[i], byte)
        }
    }
}