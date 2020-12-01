package pw.binom.io

import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncBufferedOutputAppendableTest {

    @Test
    fun appendFlush() {
        val out1 = ByteArrayOutput()
        val output = out1.asyncOutput()
        val pool = ByteBufferPool(10)
        val writer = output.bufferedWriter(pool)
        val txt = "1234567890"
        var ex: Throwable? = null
        async {
            try {
                writer.append(txt)
                writer.append(txt)
                writer.flush()
                out1.data.flip()
                val arr = txt.toCharArray()
                assertEquals(arr.size * 2, out1.data.remaining)
                for (i in 0 until arr.size) {
                    assertEquals(
                        arr[i].toByte(),
                        out1.data[i],
                        "Expected char: ${arr[i]}. Index: $i"
                    )
                }
                for (i in arr.size until arr.size * 2) {
                    assertEquals(
                        arr[i - arr.size].toByte(),
                        out1.data[i],
                        "Expected char: ${arr[i - arr.size]}. Index: $i"
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                ex = e
            }
        }
        ex?.let { throw it }
    }
}