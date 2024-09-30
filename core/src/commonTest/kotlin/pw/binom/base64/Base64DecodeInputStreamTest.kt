package pw.binom.base64

import pw.binom.io.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class Base64DecodeInputStreamTest {
  @Test
  fun test2() {
    val txt = "SGVsbG8gd29ybGQhIQ=="
    val b = Base64.decode(txt)
    println("b=${b.decodeToString()}")
    assertEquals(13, b.size)
    assertEquals("Hello world!!", b.decodeToString())
  }

  @Test
  fun test() {
    val data = ByteBuffer(200)
    Random.nextBytes(data)
    data.clear()
    val sb = StringBuilder()
    Base64EncodeOutput(sb, padding = true).use { out ->
      if (DataTransferSize.ofSize(data.capacity) != out.write(data)) {
        fail()
      }
    }
    val reader = Base64DecodeInput(StringReader(sb.toString()))
    val readedData = ByteBuffer(data.capacity)
    assertEquals(DataTransferSize.ofSize(data.capacity), reader.read(readedData))
    data.clear()
    readedData.clear()
    (data.position until data.limit).forEach {
      assertEquals(data[it], readedData[it])
    }
    assertEquals(DataTransferSize.EMPTY, reader.read(readedData))
  }
}
