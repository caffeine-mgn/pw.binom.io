package pw.binom.compression.zlib

import pw.binom.ByteDataBuffer
import pw.binom.io.ByteArrayOutput
import pw.binom.io.closablesOf
import pw.binom.io.hold
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

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
}