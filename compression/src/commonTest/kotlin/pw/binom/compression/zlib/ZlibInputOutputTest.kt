package pw.binom.compression.zlib

import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ZlibInputOutputTest{
    @Test
    fun testSync() {
        val source =TestData.SOURCE_DATA.clone()
        source.clear()
        val compressed = ByteBuffer.alloc(TestData.SOURCE_DATA.capacity * 2)
        repeat(compressed.capacity){
            compressed.put(10)
        }
        compressed.clear()
        val def = DeflaterOutput(stream = compressed, level = 6, wrap = true, closeStream = false)
        def.write(source)
        def.close()

        compressed.flip()
        assertEquals(11, compressed.remaining)
        (compressed.position until compressed.limit).forEach {
            assertEquals(TestData.COMPRESSED[it], compressed[it])
        }

        val uncompressed = ByteBuffer.alloc(source.capacity * 2)
        val inf = InflateInput(compressed, 512, true)
        inf.read(uncompressed)
        inf.close()
        uncompressed.flip()
        assertEquals(source.capacity, uncompressed.remaining)
        source.clear()
        assertArrayEquals(source, 0, uncompressed, 0, source.capacity)
    }

    @Test
    fun testAsync() {
        async {
            val source =TestData.SOURCE_DATA.clone()
            source.clear()
            val compressed = ByteBuffer.alloc(TestData.SOURCE_DATA.capacity * 2)
            repeat(compressed.capacity){
                compressed.put(10)
            }
            compressed.clear()
            val def = AsyncDeflaterOutput(stream = compressed.asyncOutput(), level = 6, wrap = true, closeStream = false)
            def.write(source)
            def.close()

            compressed.flip()
            assertEquals(11, compressed.remaining)
            (compressed.position until compressed.limit).forEach {
                assertEquals(TestData.COMPRESSED[it], compressed[it])
            }

            val uncompressed = ByteBuffer.alloc(source.capacity * 2)
            val inf = InflateInput(compressed, 512, true)
            inf.read(uncompressed)
            inf.close()
            uncompressed.flip()
            assertEquals(source.capacity, uncompressed.remaining)
            source.clear()
            assertArrayEquals(source, 0, uncompressed, 0, source.capacity)
        }
    }
}
/*

class InflaterTest {

    @Test
    fun test() {
        val input = ByteDataBuffer.alloc(100)
        val output = ByteDataBuffer.alloc(200)
        closablesOf(input, output).hold {
            val compressed = ByteArrayOutput()
            DeflaterOutput(compressed.noCloseWrapper(), 9, wrap = false, syncFlush = false).use {
                it.write(input)
            }

            compressed.trimToSize()

            val inf = Inflater(false)


            val cur = Cursor()
            cur.inputLength = compressed.data.size
            cur.outputLength = output.size
            assertEquals(input.size, inf.inflate(cur, compressed.data, output),"Invalid result inflate data size")
            input.forEachIndexed { index, byte ->
                assertEquals(byte, output[index])
            }

        }
    }
}*/
