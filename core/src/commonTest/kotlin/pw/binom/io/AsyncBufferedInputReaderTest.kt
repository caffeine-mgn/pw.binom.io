package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AsyncBufferedInputReaderTest {
    @Test
    fun testPrintLn1() {
        val data = ByteBuffer.wrap("hello\nworld_world_world\nworld".encodeToByteArray())
        val pool = ByteBufferPool(10)

        val reader = AsyncBufferedInputReader(Charsets.UTF8, data.asyncInput(), pool, charBufferSize = 10)
        var ex: Throwable? = null
        async {
            try {
                assertEquals("hello", reader.readln())
                assertEquals("world_world_world", reader.readln())
                assertEquals("world", reader.readln())
                assertNull(reader.readln())
            } catch (e: Throwable) {
                ex = e
            }
        }
        ex?.let { throw it }
    }

    @Test
    fun testPrintLn2() {
        val data = ByteBuffer.wrap("hello\nworld_world_world\nworld".encodeToByteArray())
        val pool = ByteBufferPool(10)

        val reader = AsyncBufferedInputReader(Charsets.UTF8, data.asyncInput(), pool, charBufferSize = 512)
        var ex: Throwable? = null
        async {
            try {
                assertEquals("hello", reader.readln())
                assertEquals("world_world_world", reader.readln())
                assertEquals("world", reader.readln())
                assertNull(reader.readln())
            } catch (e: Throwable) {
                ex = e
            }
        }
        ex?.let { throw it }
    }
}