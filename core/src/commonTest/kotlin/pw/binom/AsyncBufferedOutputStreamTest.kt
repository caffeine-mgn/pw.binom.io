package pw.binom

import pw.binom.io.AsyncBufferedOutputStream
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.asAsync
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncBufferedOutputStreamTest {
    @Test
    fun test() {
        val byteOut = ByteArrayOutputStream()
        val out = byteOut.asAsync()

        async {
            val o = AsyncBufferedOutputStream(out, 50)
            o.write(Random.nextBytes(40))
            assertEquals(0, byteOut.size)
            o.write(Random.nextBytes(20))
            assertEquals(50, byteOut.size)
            o.flush()
            assertEquals(60, byteOut.size)
        }
    }
}