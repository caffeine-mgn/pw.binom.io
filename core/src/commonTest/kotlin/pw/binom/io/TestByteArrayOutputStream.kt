package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestByteArrayOutputStream {

    @Test
    fun testWrite() {
        fun build(size: Int, capacity: Int) {
            val input = ByteArray(size) { it.toByte() }
            val stream = ByteArrayOutputStream(capacity)
            stream.write(input)
            val output = stream.toByteArray()

            assertEquals(input.size, output.size)
            input.forEachIndexed { index, inputValue ->
                assertEquals(inputValue, output[index])
            }
        }

        build(size = 10, capacity = 30)
        build(size = 30, capacity = 10)
        build(size = 30, capacity = 0)
    }

    @Test
    fun `write zero bytes`() {
        val stream = ByteArrayOutputStream()
        stream.write(ByteArray(0))
    }

    @Test
    fun `test invalid length`() {
        val stream = ByteArrayOutputStream()
        try {
            stream.write(ByteArray(0), length = -1)
            fail()
        } catch (e: IndexOutOfBoundsException) {
            //NOP
        }
    }

    @Test
    fun `test empty`() {
        val d = ByteArrayOutputStream().toByteArray()
        assertEquals(0, d.size)
    }
}