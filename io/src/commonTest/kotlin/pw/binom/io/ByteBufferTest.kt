package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ByteBufferTest {
    @Test
    fun wrapTest() {
        val array = ByteArray(10) { it.toByte() }
        val buf = array.wrap()
        for (i in 0 until buf.capacity) {
            assertEquals(i.toByte(), buf[i])
        }
        for (i in 0 until buf.capacity) {
            buf[i] = (i + 1).toByte()
        }
        for (i in 0 until array.size) {
            assertEquals((i + 1).toByte(), array[i])
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun banchmarkTest() {
        val buffer1 = ByteBuffer(1024)
        val buffer2 = ByteBuffer(1024)
        val count = 999999
        var totalTime = Duration.ZERO
        repeat(count) {
            totalTime += measureTime {
                buffer1.position = it % buffer1.limit
//                buffer1.clear()
//                buffer2.clear()
//                buffer1.read(buffer2)
            }
        }
        println("TIme: ${totalTime / count}")
    }
}
