package pw.binom

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufferTest {

    @Test
    fun reallocTest() {
        var b = ByteBuffer.alloc(100)
        b.position = 30
        b.limit = 72
        b = b.realloc(80)
        assertEquals(30, b.position)
        assertEquals(72, b.limit)
        assertEquals(80, b.capacity)
    }

    @Test
    fun reallocToMax() {
        val data = ByteArray(100)
        Random.Default.nextBytes(data)
        var source = ByteBuffer.alloc(130)
        data.forEach {
            source.put(it)
        }

        source = source.realloc(200)
        assertEquals(200, source.capacity)
        source.clear()
        data.forEachIndexed { index, byte ->
            assertEquals(byte, source[index])
        }
    }

    @Test
    fun reallocToMin() {
        val data = ByteArray(100)
        Random.Default.nextBytes(data)
        var source = ByteBuffer.alloc(130)

        data.forEach {
            source.put(it)
        }

        source = source.realloc(50)
        assertEquals(50, source.capacity)
        source.clear()
        source.forEachIndexed { i, byte ->
            assertEquals(data[i], byte)
        }
    }

    @Test
    fun writeTest() {
        val source = ByteBuffer.alloc(1024)
        source.put(120)
        source.put(-100)
        source.put(-29)
        source.put(-30)
        source.put(-62)
        source.put(7)
        source.put(0)
        source.put(18)
        source.put(72)
        source.put(1)
        source.put(45)
        source.flip()
        source.position = 2

        val dest = ByteBuffer.alloc(100)
        repeat(dest.capacity) {
            dest.put(0)
        }
        dest.clear()
        dest.position = 2
        assertEquals(9, dest.write(source))

        source.clear()
        dest.clear()
        (2 until 11).forEach {
            assertEquals(source[it], dest[it])
        }

        (11 until dest.limit).forEach {
            assertEquals(0, dest[it])
        }
    }

    @Test
    fun compactFillTest() {
        val self = ByteBuffer.alloc(10)
        repeat(self.remaining) {
            self.put(it.toByte())
        }

        self.limit = self.capacity
        self.position = 5
        self.compact()
        assertEquals(5, self.position)
        assertEquals(self.capacity, self.limit)
        self.flip()
        assertEquals(5, self[0])
        assertEquals(6, self[1])
        assertEquals(7, self[2])
        assertEquals(8, self[3])
        assertEquals(9, self[4])
        self.close()
    }

    @Test
    fun compactEmptyTest() {
        val self = ByteBuffer.alloc(10).empty()
        self.compact()
        assertEquals(0, self.position)
        assertEquals(self.capacity, self.limit)
    }
}