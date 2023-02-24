package pw.binom

import pw.binom.io.ByteBuffer
import pw.binom.io.empty
import pw.binom.io.forEachIndexed
import pw.binom.io.use
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufferTest {

    @Test
    fun reallocEmpty() {
        val bb = ByteBuffer(0)
        bb.realloc(50)
    }

    @Test
    fun putToByteArray() {
        val buf = ByteBuffer(5)
        val array = ByteArray(50)
        buf.write(array)
    }

    @Test
    fun putToByteArrayBufferOverflow() {
        val buf = ByteBuffer(5)
        val array = ByteArray(50)
        assertEquals(5, buf.write(array, length = 50))
        buf.clear()
        for (i in 0..4) {
            assertEquals(array[i], buf[0])
        }
    }

    @Test
    fun toByteArrayTest() {
        val b = ByteBuffer(5)
        repeat(b.capacity) {
            b.put(it.toByte())
        }
        b.position = 0
        b.limit = 4
        val result = b.toByteArray()
        assertEquals(4, result.size)
        result.forEachIndexed { index, byte ->
            assertEquals(b[index], byte)
        }

        assertEquals(0, b.position)
        assertEquals(4, b.limit)
    }

    @Test
    fun flipTest() {
        val b = ByteBuffer(30)
        b.put(10)
        b.put(10)
        b.flip()
        assertEquals(0, b.position)
        assertEquals(2, b.limit)
    }

    @Test
    fun cleanTest() {
        ByteBuffer(64).use {
            assertEquals(0, it.position)
            assertEquals(it.capacity, it.limit)
        }
    }

    @Test
    fun reallocTest() {
        var b = ByteBuffer(100)
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
        var source = ByteBuffer(130)
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
        var source = ByteBuffer(130)

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
        val source = ByteBuffer(1024)
        println("#1------------")
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

        val dest = ByteBuffer(100)
        repeat(dest.capacity) {
            dest.put(0)
        }
        dest.clear()
        dest.position = 2
        assertEquals(9, dest.write(source))
        println("#2------------")
        source.clear()
        dest.clear()

        (2 until 11).forEach {
            println("$it -> ${source[it]}==${dest[it]}")
        }

        (2 until 11).forEach {
            assertEquals(source[it], dest[it], "index: $it")
        }
        println("#3------------")
        (11 until dest.limit).forEach {
            assertEquals(0, dest[it])
        }
    }

    @Test
    fun compactFillTest() {
        val self = ByteBuffer(10)
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
        val self = ByteBuffer(10).empty()
        self.compact()
        assertEquals(0, self.position)
        assertEquals(self.capacity, self.limit)
    }

    @Test
    fun freeTest() {
        val buf = ByteBuffer(30)
        repeat(buf.capacity) {
            buf.put(it.toByte())
        }
        buf.clear()
        buf.position = 10
        buf.free()
        assertEquals(0, buf.position)
        assertEquals(20, buf.limit)
        buf.clear()
        repeat(20) {
            assertEquals((10 + it).toByte(), buf[it])
        }
        repeat(10) {
            assertEquals((20 + it).toByte(), buf[it + 20])
        }
    }

    @Test
    fun frozenTest() {
        val self = ByteBuffer(10)
        repeat(self.remaining) {
            self.put(it.toByte())
        }
        assertEquals(10, self.position)
    }
}
