package pw.binom.io

import pw.binom.ByteBufferPool
import kotlin.test.Test
import kotlin.test.assertEquals

class BufferedOutputAppendableTest {

    @Test
    fun appendFlush() {
        val output = ByteArrayOutput()
        val pool = ByteBufferPool(10)
        val writer = output.bufferedWriter(pool)
        val txt = "1234567890"
        writer.append(txt)
        writer.append(txt)
        writer.append("\r")
        writer.flush()
        output.data.flip()
        val arr = txt.toCharArray()
        assertEquals(arr.size * 2 + 1, output.data.remaining)
        for (i in 0 until arr.size) {
            assertEquals(
                arr[i].toByte(),
                output.data[i],
                "Expected char: ${arr[i]}. Index: $i"
            )
        }
        for (i in arr.size until arr.size * 2) {
            assertEquals(
                arr[i - arr.size].toByte(),
                output.data[i],
                "Expected char: ${arr[i - arr.size]}. Index: $i"
            )
        }

        assertEquals(
            '\r'.toByte(),
            output.data[arr.size * 2],
            "Expected char: \\r. Index: ${output.data[arr.size * 2]}"
        )
    }
}
