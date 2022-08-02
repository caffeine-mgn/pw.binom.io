package pw.binom.compression.zlib

import kotlinx.coroutines.runBlocking
import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.clone
import kotlin.test.Test
import kotlin.test.assertEquals

class GZipInputOutputTest {
    @Test
    fun testSync() {
        val source = TestData.SOURCE_DATA.clone()
        source.clear()
        val compressed = ByteBuffer.alloc(TestData.SOURCE_DATA.capacity * 2)
        repeat(compressed.capacity) {
            compressed.put(10)
        }
        compressed.clear()
        val def = GZIPOutput(stream = compressed, level = 6, closeStream = false)
        def.write(source)
        def.close()

        compressed.flip()
        println("Total Wrote: ${compressed.remaining}")
        assertEquals(23, compressed.remaining)

        val uncompressed = ByteBuffer.alloc(source.capacity * 2)
        val inf = GZIPInput(compressed, 512)
        inf.read(uncompressed)
        inf.close()
        uncompressed.flip()
        assertEquals(source.capacity, uncompressed.remaining)
        source.clear()
        println("r=${compressed.remaining}")
        assertArrayEquals(source, 0, uncompressed, 0, source.capacity)
    }

    @Test
    fun testAsync() = runBlocking {
        val source = TestData.SOURCE_DATA.clone()
        source.clear()
        val compressed = ByteBuffer.alloc(TestData.SOURCE_DATA.capacity * 2)
        repeat(compressed.capacity) {
            compressed.put(10)
        }
        compressed.clear()
        val def = AsyncGZIPOutput(stream = compressed.asyncOutput(), level = 6, closeStream = false)
        def.write(source)
        def.asyncClose()

        compressed.flip()
        assertEquals(23, compressed.remaining)

        val uncompressed = ByteBuffer.alloc(source.capacity * 2)
        val inf = AsyncGZIPInput(compressed.asyncInput(), 512)
        inf.read(uncompressed)
        inf.asyncClose()
        uncompressed.flip()
        assertEquals(source.capacity, uncompressed.remaining)
        source.clear()
        assertArrayEquals(source, 0, uncompressed, 0, source.capacity)
    }
}
