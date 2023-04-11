package pw.binom.io

import org.khronos.webgl.Int8Array
import org.khronos.webgl.set
import kotlin.random.Random
import kotlin.test.Test

class JsByteBufferTest {
    @Test
    fun readToInt8ArrayTest() {
        val r = Random.nextBytes(100)
        val buffer = Int8Array(100)
        repeat(buffer.length) {
            buffer[it] = r[it]
        }

        val b = ByteBuffer(buffer)
        b.limit = 50
        val readed = b.readToInt8Array()
        println("--->$readed ${readed.length}")
    }
}
