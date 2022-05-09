package pw.binom.io

import pw.binom.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ComposeReaderTest {

    @Test
    fun readByOne() {
        val r = ComposeReader()
        r.addLast(StringReader("1234"))
        r.addLast(StringReader("5678"))

        for (i in '1'..'8') {
            assertEquals(i, r.read())
        }
        assertNull(r.read())
    }

    @Test
    fun readByPart() {
        val r = ComposeReader()
        r.addLast(StringReader("1234"))
        r.addLast(StringReader("5678"))

        val v = CharArray(3)
        assertEquals(3, r.read(v))
        assertEquals('1', v[0])
        assertEquals('2', v[1])
        assertEquals('3', v[2])

        assertEquals(3, r.read(v))
        assertEquals('4', v[0])
        assertEquals('5', v[1])
        assertEquals('6', v[2])

        assertEquals(2, r.read(v))
        assertEquals('7', v[0])
        assertEquals('8', v[1])

        assertEquals(0, r.read(v))
    }

    @OptIn(ExperimentalTime::class)
    fun speedTest(name: String, count: Int, func: () -> Unit) {
        val d = measureTime {
            repeat(count) {
                func()
            }
        }
        println("$name: ${d / count}")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun speedTest() {

        val sb = StringBuilder()
        repeat(1000) {
            sb.append(UUID.toString())
        }
        val str = sb.toString()

        speedTest("ReadByOne", 100) {
            val r = ComposeReader()
            r.addLast(StringReader(str))
            r.addLast(StringReader(str))
            r.addLast(StringReader(str))

            while (r.read() != null) {
            }
        }

        val c = CharArray(30)

        speedTest("ReadByPart", 100) {
            val r = ComposeReader()
            r.addLast(StringReader(str))
            r.addLast(StringReader(str))
            r.addLast(StringReader(str))

            while (true) {
                if (r.read(c) <= 0)
                    break
            }
        }
    }
}
