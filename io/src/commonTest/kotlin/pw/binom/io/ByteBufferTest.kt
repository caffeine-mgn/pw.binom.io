package pw.binom.io

import pw.binom.testing.Testing
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

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

  @Test
  fun readIntoTest() {
    Testing.sync {
      val buffer = ByteBuffer(30)
      Random.nextBytes(buffer)
      dispose { buffer.close() }
      test("Read small") {
        val array = ByteArray(10)
        assertEquals(array.size, buffer.readInto(array))
        array.forEachIndexed { index, byte ->
          assertEquals(buffer[index], byte)
        }
      }
      test("'length' more than ByteArray.size") {
        val array = ByteArray(10)
        assertEquals(array.size, buffer.readInto(array, length = array.size * 10))
        array.forEachIndexed { index, byte ->
          assertEquals(buffer[index], byte)
        }
      }
      test("Read closed") {
        val b = ByteBuffer(10)
        b.close()
        assertEquals(0, b.readInto(ByteArray(10)))
      }
    }
  }

  @Test
  fun readTest() = Testing.sync {
    val buffer = ByteBuffer(30)
    dispose { buffer.close() }
    test("Write small") {
      val data = Random.nextBytes(10)
      buffer.write(data)
      assertEquals(10, buffer.position)
      data.forEachIndexed { index, byte ->
        assertEquals(byte, buffer[index])
      }
      assertEquals(buffer.capacity, buffer.limit)
    }

    test("Invalid `length` of ByteArray") {
      val data = Random.nextBytes(10)
      buffer.write(data, length = data.size * 3)
      assertEquals(10, buffer.position)
      data.forEachIndexed { index, byte ->
        assertEquals(byte, buffer[index])
      }
      assertEquals(buffer.capacity, buffer.limit)
    }
    test("Write to closed") {
      val b = ByteBuffer(10)
      b.close()
      assertEquals(0, b.write(ByteArray(10)))
    }
  }

  @Test
  fun forEachTest() {
    ByteBuffer(100).use { buf ->
      var count = 0
      buf.forEach {
        count++
      }
      assertEquals(buf.capacity, count)
    }
  }

  @Test
  fun forEachIndexedTest() {
    ByteBuffer(100).use { buf ->
      var count = 0
      buf.forEachIndexed { index, value ->
        count++
      }
      assertEquals(buf.capacity, count)
    }
  }
}
