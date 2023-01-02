package pw.binom.io.http

import kotlinx.coroutines.runBlocking
import pw.binom.*
import pw.binom.io.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsyncMultipartInputTest {

    @Test
    fun findEndTest() {
        val txt = "--SPLIT\r\n\r\nANT\r\n--SPLIT\r\n\r\nON\r\n--SPLIT--\r\n"
            .toByteBufferUTF8()

        val endState = EndState()

        assertTrue(findEnd("SPLIT", txt, endState))
        assertEquals(14, endState.limit)
        assertEquals(11, endState.skip)
        assertEquals(EndState.Type.BLOCK_EOF, endState.type)

        txt.position = 14 + 11

        assertTrue(findEnd("SPLIT", txt, endState))
        assertEquals(29, endState.limit)
        assertEquals(13, endState.skip)
        assertEquals(EndState.Type.DATA_EOF, endState.type)
    }

    @Test
    fun asyncMultipartInputTest() {
        val stream = ByteArrayOutput()
        val mulipart = AsyncMultipartOutput(stream.asyncOutput(), closeParent = false)
        var exception: Throwable? = null
        val userName = Random.nextUuid().toString()
        val userPassword = Random.nextUuid().toString()
        val bufferPool = ByteBufferPool(100)
        runBlocking {
            try {
                // -------build test data-------//
                mulipart.formData("userName")
                mulipart.utf8Appendable().append(userName)
                mulipart.formData("userPassword")
                mulipart.utf8Appendable().append(userPassword)
                mulipart.formData("emptyData")
                mulipart.asyncClose()
                stream.trimToSize()
                stream.data.clear()
                val testData = stream.data.clone()
                stream.close()

                // -------test-------//
                val t = ByteBuffer(100)
                val input = AsyncMultipartInput(
                    separator = mulipart.boundary,
                    stream = testData.asyncInput(),
                    bufferPool = bufferPool
                )

                t.clear()
                assertEquals(0, input.read(t))
                assertTrue(input.next())
                assertEquals("userName", input.formName)
                assertEquals(userName, input.utf8Reader().readText())

                assertTrue(input.next())
                assertEquals("userPassword", input.formName)
                assertEquals(userPassword, input.utf8Reader().readText())
                assertTrue(input.next())
                assertEquals("emptyData", input.formName)
                t.clear()
                assertEquals(0, input.read(t))
                assertFalse(input.next())
            } catch (e: Throwable) {
                exception = e
            }
        }

        if (exception != null) {
            throw exception!!
        }
    }

    /*
    @Test
    fun ReaderWithSeparatorTest() {
        val txt = "ANT\r\n--SPLIT\r\n\r\nONQ\r\n--SPLIT--\r\n"
                .toByteBufferUTF8()

        txt.print()

        val buffer = ByteBufferPool(15u)
        val reader = ReaderWithSeparator("SPLIT", txt.asyncInput(), buffer)
        var exception: Throwable? = null
        val t = ByteBuffer(50)
        async {
            try {
                assertEquals(0, reader.read(t))
                assertTrue(reader.next())
                assertEquals(3, reader.read(t))
                t.clear()
                assertEquals('A'.toByte(), t[0])
                assertEquals('N'.toByte(), t[1])
                assertEquals('T'.toByte(), t[2])
                t.clear()
                assertEquals(0, reader.read(t))
                assertTrue(reader.next())
//                assertEquals(2, reader.read(t))
                reader.read(t)
                t.flip()
                (t.position until t.limit).forEach {
                    println("$it -> ${t[it].toChar2()}")
                }
                assertEquals('\r'.toByte(), t[0])
                assertEquals('\n'.toByte(), t[1])
                assertEquals('O'.toByte(), t[2])
                assertEquals('N'.toByte(), t[3])
                assertEquals('Q'.toByte(), t[4])
                t.clear()
                assertEquals(0, reader.read(t))
                assertFalse(reader.next())
//                t.flip()
//                (t.position until t.limit).forEach {
//                    println("$it -> ${t[it].toChar2()}")
//                }
//                assertEquals(0, reader.read(t))
            } catch (e: Throwable) {
                exception = e
            }
        }

        if (exception != null)
            throw exception!!
    }
*/
    /*
        @OptIn(ExperimentalStdlibApi::class)
        @Test
        fun ff() {
            val txt = "--SPLIT\r\n\r\nANT\r\n--SPLIT\r\n\r\nON--SPLIT--\r\n"
            val reader = MultipartReader(txt.toByteBufferUTF8().asyncInput(), "SPLIT")
            val b = ByteBuffer(1)
            var exception: Throwable? = null
            async {
                try {
                    reader.next()
                    assertEquals(1, reader.read(b))
                    b.clear()
                    assertEquals('A'.toByte(), b[0])
                    assertEquals(1, reader.read(b))
                    b.clear()
                    assertEquals('N'.toByte(), b[0])
                    assertEquals(1, reader.read(b))
                    b.clear()
                    assertEquals('T'.toByte(), b[0])
                    b.clear()
                    assertEquals(0, reader.read(b))
                    b.clear()
                    assertEquals(0, reader.read(b))
                } catch (e: Throwable) {
                    exception = e
                }
            }

            if (exception != null)
                throw exception!!
        }

        @Ignore
        @Test
        fun test() {
            val endLine = "\r\n"
            val spillter = "SPLIT"
            val buffer = ("--$spillter\r\n" +
                    "Content-Disposition: form-data; name=\"hello\"$endLine" +
                    "$endLine" +
                    "123$endLine" +
                    "--$spillter$endLine" +
                    "Content-Disposition: form-data; name=\"myfile\"; filename=\"1.txt\"\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "File Body!\r\n" +
                    "--$spillter--\r\n" +
                    "\r\n").toByteBufferUTF8()

            println(buffer.toByteArray().toList().map { "0x${it.toString(16)}" }.joinToString(" "))
            buffer.clear()
            val reader = MultipartReader(buffer.asyncInput(), spillter)
            var exception: Throwable? = null
            async {
                try {
                    println("#1 Try next")
                    assertTrue(reader.next())
                    println("#1 Try ready body")
                    assertEquals("123", reader.utf8Reader().readText())
                    println("#2 Try next")
                    assertTrue(reader.next())
                    println("#2 Try ready body")
                    assertEquals("File Body!", reader.utf8Reader().readText())
                    println("#3 Try next")
                    assertFalse(reader.next())
                    println("#3 End!")
                } catch (e: Throwable) {
                    exception = e
                    e.printStacktrace()
                }
            }
            if (exception != null)
                throw exception!!
        }
        */
}
