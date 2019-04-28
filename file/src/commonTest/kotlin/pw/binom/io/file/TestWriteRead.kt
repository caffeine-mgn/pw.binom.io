package pw.binom.io.file

import pw.binom.asUTF8ByteArray
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWriteRead {

    @Test
    fun `read write`() {
        val f = File("test")

        val data=byteArrayOf(0x0A ,-0x32 ,-0x34 ,0x2D ,-0x38 ,0x49 ,0x55 ,0x08,0x49, -0x53 ,0x28 ,0x01 ,0x00 ,0x00 ,0x00 ,-0x1,-0x1)

        FileOutputStream(f, false).use {
            it.write(data)
        }

        FileInputStream(f).use {
            val input = ByteArray(data.size*2)
            assertEquals(data.size, it.read(input))
            for (i in 0 until data.size)
                assertEquals(data[i], input[i])
        }
        f.delete()
    }
}