package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTest {

//    class TestWebSocketConnection(override val masking: Boolean, input: AsyncInput, output: AsyncOutput) : AbstractWebSocketConnection(
//            input = input,
//            output = output,
//    )
//
//    @Test
//    fun test() {
//        val buf = ByteArrayOutput()
//        val str = Random.uuid().toString()
//        async {
//            println("--------WRITE--------")
//            WSOutput(
//                    messageType = MessageType.TEXT,
//                    masked = true,
//                    stream = buf.asyncOutput(),
//                    bufferSize = DEFAULT_BUFFER_SIZE
//            ).utf8Appendable().use {
//                it.append(str)
//            }
//            println("--------WRITE--------")
//            println("--------READ--------")
//            buf.data.flip()
//            TestWebSocketConnection(false, buf.data.asyncInput(), buf.asyncOutput())
//                    .read().utf8Reader().use {
//                        assertEquals(str, it.readText())
//                    }
//            println("--------READ--------")
//        }
//    }

    @Test
    fun encodeTest() {
        val data = ByteArray(100)
        Random.nextBytes(data)
        val buf = ByteBuffer.alloc(data.size)
        data.forEach {
            buf.put(it)
        }
        buf.clear()
        val mask = Random.nextInt()
        Message.encode(0uL, mask, buf)
        buf.clear()
        Message.encode(0uL, mask, buf)
        buf.clear()
        buf.forEachIndexed { i, byte ->
            assertEquals(data[i], byte)
        }
    }
}