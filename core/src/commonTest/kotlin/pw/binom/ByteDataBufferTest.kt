package pw.binom

import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ByteDataBufferTest {

    @Test
    fun test() {
        val d = ByteDataBuffer.alloc(10)
        d[5] = 127
        assertEquals(127, d[5])
        d.close()
    }

    @Test
    fun testWrap() {
        val data = ByteArray(100)
        Random.nextBytes(data)
        val buffer = ByteDataBuffer.wrap(data)
        data.forEachIndexed { index, byte ->
            assertEquals(byte, buffer[index])
        }
        data.fill(42)

        data.forEachIndexed { index, byte ->
            assertEquals(byte, buffer[index])
        }
    }

//    @OptIn(ExperimentalTime::class)
//    @Ignore
//    @Test
//    fun banchMarck() {
//        val times = 100
//        val size = 100000
//        val timeArray = measureTime {
//            val source = ByteArray(size)
//            val destination = ByteArray(size)
//            repeat(times) {
//                source.forEachIndexed { index, byte ->
//                    destination[index] = byte
//                }
//            }
//        }
//
//        val timeBufferSafe = measureTime {
//            val source = ByteDataBuffer.alloc(size)
//            val destination = ByteDataBuffer.alloc(size)
//            repeat(times) {
//                source.forEachIndexed { index, byte ->
//                    destination[index] = byte
//                }
//            }
//        }
//
//        val timeBufferUnsafe = measureTime {
//            val source = ByteDataBuffer.alloc(size)
//            val destination = ByteDataBuffer.alloc(size)
//            repeat(times) {
//                source.unsafe { source ->
//                    destination.unsafe { destination ->
//                        source.forEachIndexed { index, byte ->
//                            destination[index] = byte
//                        }
//                    }
//                }
//            }
//        }
//
//        val timeCycle = measureTime {
//            repeat(times * size) {
//            }
//        }
//
//        println("Time timeArray: $timeArray")
//        println("Time timeBufferSafe: $timeArray")
//        println("Time timeBufferUnsafe: $timeArray")
//        println("Time timeCycle: $timeCycle")
//    }

    @Test
    fun allocTest() {
        val d = ByteDataBuffer.alloc(10) { (it * 2).toByte() }
        (0 until 10).forEach {
            assertEquals(it * 2, d[it].toInt())
        }
        d.forEachIndexed { index, byte ->
            assertEquals(index * 2, byte.toInt())
        }
        assertEquals(10, d.size)
    }

    @Test
    fun wrapTest() {
        val d = ByteDataBuffer.wrap(byteArrayOf(10, 20, 30))
        assertEquals(10, d[0])
        assertEquals(20, d[1])
        assertEquals(30, d[2])
        assertEquals(3, d.size)
    }

    @Test
    fun copyToTest() {
        val s = ByteDataBuffer.alloc(8) { (it + 11).toByte() }
        val d = ByteDataBuffer.alloc(18) { 0 }
        s.copyInto(d)

        (0 until s.size).forEach {
            assertEquals(s[it], d[it])
        }
        (s.size + 1 until d.size).forEach {
            assertEquals(0, d[it])
        }
    }

    @Test
    fun forEachTest() {
        val d = ByteDataBuffer.alloc(10) { it.toByte() }
        var c = 0
        d.forEach {
            assertEquals(c.toByte(), it)
            c++
        }
        assertEquals(c, 10)

        c = 0

        d.forEachIndexed { index, byte ->
            assertEquals(c, index)
            assertEquals(c.toByte(), byte)
            c++
        }
        assertEquals(c, 10)
    }

    @Test
    fun copyIntoTest() {
        val s = ByteDataBuffer.alloc(10) { it.toByte() }

        (0 until 10).forEach {
            println("$it->${s[it]}")
        }

        val d = ByteDataBuffer.alloc(5)
        s.copyInto(d, 0, 0, 4)

        d.forEachIndexed { index, byte ->
            if (index <= 3)
                assertEquals(index.toByte(), byte, "Invalid in index $index")
        }

    }
}