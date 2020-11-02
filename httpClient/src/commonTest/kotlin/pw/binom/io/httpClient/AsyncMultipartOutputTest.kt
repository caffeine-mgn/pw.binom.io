package pw.binom.io.httpClient

import pw.binom.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.httpServer.AsyncMultipartInput
import pw.binom.io.readText
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsyncMultipartOutputTest {

    @Test
    fun test() {
        val stream = ByteArrayOutput()
        val mulipart = AsyncMultipartOutput(stream.asyncOutput())
        var exception: Throwable? = null
        val userName = UUID.random().toString()
        val userPassword = UUID.random().toString()
        val bufferPool = ByteBufferPool(10,100u)
        async {
            try {
                mulipart.part("userName")
                mulipart.utf8Appendable().append(userName)
                mulipart.part("userPassword")
                mulipart.utf8Appendable().append(userPassword)
                mulipart.close()

                stream.data.flip()
                val input = AsyncMultipartInput(mulipart.boundary, stream.data.asyncInput(), bufferPool)
                assertTrue(input.next())
                assertEquals("userName", input.formName)
                assertEquals(userName, input.utf8Reader().readText())
                assertTrue(input.next())
                assertEquals("userPassword", input.formName)
                assertEquals(userPassword, input.utf8Reader().readText())
                assertFalse(input.next())
            } catch (e: Throwable) {
                exception = e
            }
        }

        if (exception != null)
            throw exception!!
    }
}