package pw.binom.base64

import pw.binom.io.StringReader
import pw.binom.io.use
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class Base64DecodeInputStreamTest {

    @Test
    fun test() {
        val data = Random.nextBytes(200)
        val sb = StringBuilder()
        Base64EncodeOutput(sb).use { out ->
            if (data.size != out.write(data))
                fail()
        }
        val reader = Base64DecodeInput(StringReader(sb.toString()))
        val readedData = ByteArray(data.size)
        assertEquals(data.size, reader.read(readedData))
        data.forEachIndexed { index, byte ->
            println("-->$index")
            assertEquals(byte, readedData[index])
        }
        assertEquals(0, reader.read(readedData))
    }
}