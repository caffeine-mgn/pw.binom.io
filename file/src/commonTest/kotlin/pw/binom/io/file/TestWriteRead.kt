package pw.binom.io.file

import pw.binom.asUTF8ByteArray
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWriteRead {

    @Test
    fun `read write`() {
        val f = File("test")
        val data = "12345".asUTF8ByteArray()

        FileOutputStream(f, false).use {
            it.write(data)
        }

        FileInputStream(f).use {
            val input = ByteArray(10)
            assertEquals(5, it.read(input))
            for (i in 0 until 5)
                assertEquals(data[i], input[i])
        }
        f.delete()
    }
}