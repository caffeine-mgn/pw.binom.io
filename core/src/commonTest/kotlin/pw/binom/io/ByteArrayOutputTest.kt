package pw.binom.io

import pw.binom.System
import pw.binom.writeByte
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteArrayOutputTest {

    @Test
    fun toByteArrayTest() {
        val b = ByteArrayOutput()
        b.writeByte(10)
        b.writeByte(20)
        b.writeByte(30)
        b.writeByte(40)
        val r = b.toByteArray()
        assertEquals(4, r.size)
        assertEquals(10, r[0])
        assertEquals(20, r[1])
        assertEquals(30, r[2])
        assertEquals(40, r[3])
    }

    @Test
    fun reallocBigPartTest() {
        val out = ByteArrayOutput(
            capacity = 512,
            capacityFactor = 1.7f
        )

        val pp = ByteBuffer(1024)
        Random.nextBytes(pp)
        pp.clear()
        out.write(pp)

        assertEquals(1024, out.data.capacity)
        assertEquals(0, out.data.remaining)

        out.data.clear()
        println("-->out.data=${out.data.hashCode()}")
        println("-->${out.data[0]}")
        var kk = out.data
        System.gc()
        for (i in kk.position until kk.limit) {
            println("1->$i   ${kk.hashCode()}")
        }
        out.data.forEachIndexed { i, byte ->
            println("2->$i   ${out.data.hashCode()}")
//            println("->$byte")
//            assertEquals(pp[i], byte)
        }
        println("Test done! $out $kk")
//        out.trimToSize()
//        out.data.clear()
//        println("--->${out.data[0]}")
//        out.data.forEachIndexed { i, byte ->
//            assertEquals(pp[i], byte)
//        }
    }

    @Test
    fun reallocShortPartTest() {
        val out = ByteArrayOutput(
            capacity = 10,
            capacityFactor = 1.7f
        )
        val tmp = ByteBuffer(1)
        val pp = ByteBuffer(50)
        repeat(pp.capacity) {
            pp.put(it.toByte())
        }
        pp.clear()
        pp.forEach {
            out.writeByte(tmp, it)
        }

//        assertEquals(1024, out.data.capacity)
//        assertEquals(0, out.data.remaining)
        assertEquals(pp.capacity, out.size)
        out.data.flip()
        out.data.forEachIndexed { i, byte ->
            assertEquals(pp[i], byte)
        }
        out.trimToSize()
        out.data.clear()
        out.data.forEachIndexed { i, byte ->
            assertEquals(pp[i], byte)
        }
    }
}
