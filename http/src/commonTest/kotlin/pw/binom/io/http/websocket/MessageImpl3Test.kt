package pw.binom.io.http.websocket

import kotlinx.coroutines.test.runTest
import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.copyTo
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MessageImpl3Test {
  @Test
  fun reading() =
    runTest {
      val data1 = Random.nextBytes(30)
      val data2 = ByteArray(0)
      val data3 = Random.nextBytes(1024 * 1024)
      val total = data1 + data2 + data3
      val buf = ByteArrayOutput()
      WebSocketHeader.write(
        output = buf.asyncOutput(),
        length = data1.size.toLong(),
        opcode = Opcode.BINARY,
      )
      buf.write(data1)

      WebSocketHeader.write(
        output = buf.asyncOutput(),
        length = data2.size.toLong(),
      )
      buf.write(data2)

      WebSocketHeader.write(
        output = buf.asyncOutput(),
        length = data3.size.toLong(),
        finishFlag = true,
      )
      buf.write(data3)
      val wasRead = ByteArrayOutput()
      val msg = WebSocketInputImpl(ByteArrayInput(buf.toByteArray()).asyncInput())
      msg.startMessage()
      ByteBuffer(20).use {
        // reading first part `data1`
        assertEquals(20, msg.read(it))
        it.flip()
        wasRead.write(it)

        // reading remain ща first part `data1`
        it.clear()
        assertEquals(10, msg.read(it))
        it.flip()
        wasRead.write(it)

        // `data2` should be auto skip and starts reading `data3`
        it.clear()
        assertEquals(it.capacity, msg.read(it))
        it.flip()
        wasRead.write(it)
      }
      msg.copyTo(wasRead.asyncOutput())
      assertContentEquals(total, wasRead.toByteArray())
    }
}
