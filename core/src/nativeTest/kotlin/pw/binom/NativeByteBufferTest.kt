package pw.binom

import kotlin.test.Test

class NativeByteBufferTest {

    @Test
    fun refTest() {
        val buf = ByteBuffer.alloc(50)
        buf.clear()
        buf.ref { cPointer, i ->
        }
        buf.empty()
        buf.ref { cPointer, i ->
        }
    }
}
