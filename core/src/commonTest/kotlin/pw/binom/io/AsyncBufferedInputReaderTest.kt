package pw.binom.io

import kotlinx.coroutines.test.runTest
import pw.binom.*
import pw.binom.charset.Charsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AsyncBufferedInputReaderTest {
    @Test
    fun testPrintLn1() {
        val data = ByteBuffer.wrap("hello\nworld_world_world\nworld".encodeToByteArray())
        val pool = ByteBufferPool(1000)

        val reader = AsyncBufferedInputReader(Charsets.UTF8, data.asyncInput(), pool, charBufferSize = 10)
        var ex: Throwable? = null
        runTest {
            try {
                assertEquals("hello", reader.readln(), "error on \"hello\"")
                assertEquals("world_world_world", reader.readln(), "error on \"world_world_world\"")
                assertEquals("world", reader.readln(), "error on \"world\"")
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

        val reader = data.asyncInput().bufferedReader(charset = Charsets.UTF8)
        var ex: Throwable? = null
        runTest {
            try {
                val hello = reader.readln()!!
                assertEquals("hello", hello)
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