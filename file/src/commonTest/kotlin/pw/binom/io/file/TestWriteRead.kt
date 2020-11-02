package pw.binom.io.file

import pw.binom.ByteBuffer
import pw.binom.io.use
import pw.binom.writeByte
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWriteRead {

    @Test
    fun `read write`() {
        val f = File("test")
        val buf = ByteBuffer.alloc(8)


        val data = byteArrayOf(
                0x0A, -0x32, -0x34, 0x2D, -0x38, 0x49, 0x55, 0x08,
                0x49, -0x53, 0x28, 0x01, 0x00, 0x00, 0x00, -0x1, -0x1
        )

        f.write().use {
            data.forEach { value ->
                it.writeByte(buf, value)
            }
        }

        f.read().use {
            val input = ByteBuffer.alloc(data.size * 2)
            assertEquals(data.size, it.read(input))
            input.flip()
            for (i in 0 until data.size)
                assertEquals(data[i], input[i])
        }
        f.delete()
    }
}