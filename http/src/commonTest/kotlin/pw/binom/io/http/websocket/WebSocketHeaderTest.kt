package pw.binom.io.http.websocket

import kotlinx.coroutines.runBlocking
import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketHeaderTest {

    @Test
    fun readTest() {
        val data = ByteBuffer.wrap(ubyteArrayOf(0x82u, 0xfeu, 0x0u, 0x82u, 0x88u, 0x4du, 0x1du, 0x84u).toByteArray())
        runBlocking {
            val header = WebSocketHeader()
            WebSocketHeader.read(data.asyncInput(), header)
            header.apply {
                assertEquals(2, opcode)
                assertEquals(130uL, length)
                assertTrue(header.maskFlag)
                assertEquals(-2008212092, mask)
                assertTrue(finishFlag)
            }
        }
    }

    @Test
    fun readWrite() {
        val rightData = ubyteArrayOf(0x82u, 0xfeu, 0x0u, 0x82u, 0x88u, 0x4du, 0x1du, 0x84u)
        val header = WebSocketHeader()
        header.apply {
            opcode = 2
            length = 130uL
            maskFlag = true
            mask = -2008212092
            finishFlag = true
        }
        val output = ByteBuffer.alloc(10)
        runBlocking {
            WebSocketHeader.write(output.asyncOutput(), header)
            output.flip()
            assertEquals(8, output.remaining)
            output.forEachIndexed { i, byte ->
                assertEquals(rightData[i].toByte(), byte)
            }
        }
    }
}