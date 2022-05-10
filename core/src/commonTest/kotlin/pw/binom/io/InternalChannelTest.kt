package pw.binom.io

import pw.binom.readLong
import pw.binom.writeLong
import kotlin.test.Test
import kotlin.test.assertEquals

class InternalChannelTest {

    @Test
    fun test() {
        val buf = ByteBuffer.alloc(16)
        val clientBuf = ByteBuffer.alloc(1024).clean()
        val serverBuf = ByteBuffer.alloc(1024).clean()

        val server = InternalChannel(readBuffer = serverBuf, writeBuffer = clientBuf)
        val client = InternalChannel(readBuffer = clientBuf, writeBuffer = serverBuf)

        println("#1")
        server.writeLong(buf, 11)
        println("#2")
        assertEquals(11, client.readLong(buf))
        println("#3")
    }
}
