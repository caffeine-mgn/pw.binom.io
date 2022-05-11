package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class ZlibTest {

    @Test
    fun test() {
        val sourceData = ByteBuffer.alloc(30)
        repeat(sourceData.capacity) {
            sourceData.put(10)
        }
        sourceData.clear()
        val compressed = ByteBuffer.alloc(sourceData.capacity * 2)
        val def = Deflater(6, true, true)

        while (true) {
            if (def.deflate(sourceData, compressed) <= 0)
                break
        }
        while (true) {
            if (!def.flush(compressed))
                break
        }
        def.finish()
        while (true) {
            if (!def.flush(compressed))
                break
        }
        compressed.flip()
        (compressed.position until compressed.limit).forEach {
            assertEquals(TestData.COMPRESSED[it], compressed[it])
        }
        println("compressed size: ${compressed.remaining}")
        assertEquals(11, compressed.remaining)

        (compressed.position until compressed.limit).forEach {
            println("$it->${compressed[it]}")
        }

        val inf = Inflater()
        val uncompressed = ByteBuffer.alloc(sourceData.capacity * 2)
        while (true) {
            if (inf.inflate(compressed, uncompressed) <= 0)
                break
        }
        inf.end()

        uncompressed.flip()
        assertEquals(sourceData.capacity, uncompressed.remaining)
        assertArrayEquals(sourceData, 0, uncompressed, 0, sourceData.capacity)
    }
}

fun assertArrayEquals(expected: ByteBuffer, expectedOffset: Int, actual: ByteBuffer, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}
