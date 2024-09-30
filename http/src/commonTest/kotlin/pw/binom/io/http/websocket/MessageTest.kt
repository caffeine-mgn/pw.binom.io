package pw.binom.io.http.websocket

import pw.binom.io.ByteBuffer
import pw.binom.io.wrap
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

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun encodeTest2() {
    val data = ByteArray(50) { it.toByte() }
    val mask = 1234
    val bytes = data.wrap {
      WebSocketInput.encode(cursor = 0L, mask = mask, data = it)
      assertEquals(it.capacity, it.limit)
      assertEquals(it.capacity, it.position)
      it.clear()
      it.toByteArray()
    }
    assertEquals(
      "000106d1040502d508090ed90c0d0add101116c1141512c518191ec91c1d1acd202126f1242522f528292ef92c2d2afd3031",
      bytes.toHexString()
    )
  }

  @Test
  fun encodeTest() {
    val data = ByteArray(100)
    Random.nextBytes(data)
    val buf = ByteBuffer(data.size)
    data.forEach {
      buf.put(it)
    }
    buf.clear()
    val mask = Random.nextInt()
    WebSocketInput.encode(cursor = 0L, mask = mask, data = buf)
    buf.clear()
    WebSocketInput.encode(cursor = 0L, mask = mask, data = buf)
    buf.clear()
    buf.forEachIndexed { i, byte ->
      assertEquals(data[i], byte)
    }
  }
}
