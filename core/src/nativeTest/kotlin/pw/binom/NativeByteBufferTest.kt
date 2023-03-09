package pw.binom

import pw.binom.io.ByteBuffer
import pw.binom.io.empty
import kotlin.test.Test

class NativeByteBufferTest {

    @Test
    fun refTest() {
        val buf = ByteBuffer(50)
        buf.clear()
        buf.ref(0) { cPointer, i ->
        }
        buf.empty()
        buf.ref(0) { cPointer, i ->
        }
    }
}
