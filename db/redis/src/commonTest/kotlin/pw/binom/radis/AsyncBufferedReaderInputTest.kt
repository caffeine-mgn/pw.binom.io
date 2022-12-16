package pw.binom.radis

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pw.binom.asyncInput
import pw.binom.db.radis.AsyncBufferedReaderInput
import pw.binom.io.ByteArrayOutput
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncBufferedReaderInputTest {
    @Test
    fun readTextLen() = runTest {
        suspend fun test(len: Int) {
            try {
                val data = ByteArrayOutput()
                val l1 = "ANTON-1-2-3"
                val l2 = "ARTEM-4-5-6"
                data.write(l1.encodeToByteArray())
                data.write(l2.encodeToByteArray())
                val s = AsyncBufferedReaderInput(data.lock().asyncInput(), bufferSize = len, closeParent = true)
                println("READ ANTON")
                assertEquals(l1, s.readString(length = l1.length))
                println("READ ARTEM")
                assertEquals(l2, s.readString(length = l2.length))
            } catch (e: Throwable) {
//                throw e
                throw RuntimeException("Error on length $len", e)
            }
        }
        test(10)
//        test(1024)
    }

    @Test
    fun readlnTest() = runTest {
        suspend fun test(len: Int) {
            val data = ByteArrayOutput()
            val l1 = Random.nextUuid().toString()
            val l2 = Random.nextUuid().toString()
            data.write(l1.encodeToByteArray())
            data.write("\r\n".encodeToByteArray())
            data.write(l2.encodeToByteArray())
            data.write("\r\n".encodeToByteArray())
            val s = AsyncBufferedReaderInput(data.lock().asyncInput(), bufferSize = len, closeParent = true)
            assertEquals(l1, s.readln())
            assertEquals(l2, s.readln())
        }
        test(10)
        test(1024)
    }

    @Test
    fun readCharTest() = runTest {
        suspend fun test(len: Int) {
            val data = ByteArrayOutput()
            val l1 = "Ab"
            val l2 = "Cd"
            data.write(l1.encodeToByteArray())
            data.write("\r\n".encodeToByteArray())
            data.write(l2.encodeToByteArray())
            data.write("\r\n".encodeToByteArray())
            val s = AsyncBufferedReaderInput(data.lock().asyncInput(), bufferSize = len, closeParent = true)
            assertEquals('A', s.readANSIChar())
            assertEquals('b', s.readANSIChar())
            s.skipCRLF()
            assertEquals('C', s.readANSIChar())
            assertEquals('d', s.readANSIChar())
            s.skipCRLF()
        }
        test(10)
        test(1024)
    }
}
