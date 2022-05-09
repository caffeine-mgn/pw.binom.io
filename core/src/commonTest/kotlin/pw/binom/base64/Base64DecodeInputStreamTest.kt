package pw.binom.base64

import pw.binom.ByteBuffer
import pw.binom.io.StringReader
import pw.binom.io.use
import pw.binom.nextBytes
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class Base64DecodeInputStreamTest {

    @Test
    fun test2() {
        val txt = "c3Vib2NoZXY6RHJvdm9zZWszMTk="
        val b = Base64.decode(txt)
        println("b=${b.decodeToString()}")
    }

    @Test
    fun test() {
        val data = ByteBuffer.alloc(200)
        Random.nextBytes(data)
        data.clear()
        val sb = StringBuilder()
        Base64EncodeOutput(sb).use { out ->
            if (data.capacity != out.write(data))
                fail()
        }
        val reader = Base64DecodeInput(StringReader(sb.toString()))
        val readedData = ByteBuffer.alloc(data.capacity)
        assertEquals(data.capacity, reader.read(readedData))
        data.clear()
        readedData.clear()
        (data.position until data.limit).forEach {
            assertEquals(data[it], readedData[it])
        }
        assertEquals(0, reader.read(readedData))
    }
}
