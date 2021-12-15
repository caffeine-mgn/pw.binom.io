package pw.binom.io

import pw.binom.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class TestAsyncBufferedInput {

    @Test
    fun test() {
        val data = ByteBuffer.alloc(256)
        Random.nextBytes(data)
        data.clear()
        val b = data.asyncInput().bufferedInput(10)

        runBlocking {
            val buf = ByteBuffer.alloc(50)
            assertEquals(-1, b.available)
            buf.reset(0,5)
            assertEquals(5, b.readFully(buf))
            assertEquals(5, b.available)
            assertArrayEquals(data, 0, buf, 0, 5)
            buf.reset(0,5)
            assertEquals(5, b.readFully(buf))
            assertArrayEquals(data, 5, buf, 0, 5)
            buf.reset(0,10)
            assertEquals(10, b.readFully(buf))
            assertArrayEquals(data, 10, buf, 0, 5)
        }
    }
}