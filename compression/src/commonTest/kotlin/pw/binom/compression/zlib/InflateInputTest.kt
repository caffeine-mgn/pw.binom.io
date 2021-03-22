package pw.binom.compression.zlib

import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals

class InflateInputTest {

    @Test
    fun asyncTest(){
        async2 {
            val source = TestData.SOURCE_DATA.clone()
            source.clear()
            val compressed = ByteBuffer.alloc(TestData.SOURCE_DATA.capacity * 2)
            repeat(compressed.capacity) {
                compressed.put(10)
            }
            compressed.clear()
            val def = AsyncDeflaterOutput(stream = compressed.asyncOutput(), level = 6, closeStream = false)
            def.write(source)
            def.asyncClose()

            compressed.flip()
            assertEquals(15, compressed.remaining)
        }.getOrException()
    }
}