package pw.binom.io

import pw.binom.UUID
import pw.binom.base64.Base64DecodeInputStream
import pw.binom.base64.Base64EncodeOutputStream
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private class SkipOutputStream : OutputStream {
    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        return length
    }

    override fun flush() {
    }

    override fun close() {
    }

}

class StringReaderTest {

    @Test
    fun `read`() {
        val reader = "123".asReader()

        assertEquals('1', reader.read())
        assertEquals('2', reader.read())
        assertEquals('3', reader.read())

        assertNull(reader.read())
    }

    @Test
    fun `out of length`() {
        val reader = "1234567890".asReader()

        val data = CharArray(20)

        reader.read(data, length = 5)
        assertEquals(5, reader.read(data, length = 10))
        assertEquals(0, reader.read(data))
    }


    @ExperimentalStdlibApi
    @OptIn(ExperimentalTime::class)
    @Test
    fun timeTest() {
        var count = 50
        var timeToReadByOne = Duration.ZERO
        var timeToReadByPart = Duration.ZERO
        var timeToConvert = Duration.ZERO
        var timeToReadBase64 = Duration.ZERO

        val base64 = run {
            val sb = StringBuilder()
            Base64EncodeOutputStream(sb).use {
                it.write(Random.nextBytes(36_000))
            }
            sb.toString()
        }

        (0 until count).forEach {
            val sb = StringBuilder()
            val timeForBuildString = measureTime {
                (0..1000).forEach {
                    sb.append(UUID.toString())
                }
            }
            val str = sb.toString()
            timeToConvert += measureTime { str.toCharArray() }
            var cc = StringReader(str)
            timeToReadByOne += measureTime {
                while (cc.read() != null) {
                    //
                }
            }

            cc = StringReader(str)
            val c = CharArray(32)
            timeToReadByPart += measureTime {
                while (true) {
                    if (cc.read(c) <= 0)
                        break
                }
            }


            timeToReadBase64 += measureTime {
                Base64DecodeInputStream(StringReader(base64)).use {
                    it.copyTo(SkipOutputStream())
                }
//                Base64DecodeInputStream(StringReader(base64)).use {
//                    while (true) {
//                        try {
//                            it.read()
//                        } catch (e: EOFException) {
//                            break
//                        }
//                    }
//                }
            }
        }
        timeToReadByOne = timeToReadByOne / count
        timeToConvert = timeToConvert / count
        timeToReadByPart = timeToReadByPart / count
        timeToReadBase64 = timeToReadBase64 / count

        println("timeToReadByOne: $timeToReadByOne")
        println("timeToConvert: $timeToConvert")
        println("timeToReadByPart: $timeToReadByPart")
        println("timeToReadBase64: $timeToReadBase64")
    }
}