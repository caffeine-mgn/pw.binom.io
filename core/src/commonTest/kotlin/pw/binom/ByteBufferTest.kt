package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufferTest {

    @Test
    fun test() {
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
    fun compactTest() {
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
    }
}