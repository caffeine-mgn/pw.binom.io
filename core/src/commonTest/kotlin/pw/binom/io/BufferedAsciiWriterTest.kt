package pw.binom.io

import pw.binom.asyncOutput
import kotlin.test.Test
import pw.binom.io.*
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class BufferedAsciiWriterTest {
    @Test
    fun test() {
        val out = ByteArrayOutput()

        runBlocking {
            out.asyncOutput().bufferedAsciiWriter(closeParent = false).use {
                it.append("Anton")
            }
        }
        assertEquals("Anton", out.toByteArray().decodeToString())
    }

    @Test
    fun test2() {
        val out = ByteArrayOutput()

        runBlocking {
            out.asyncOutput().bufferedAsciiWriter(closeParent = false).use {
                it.append("A")
            }
        }
        assertEquals("A", out.toByteArray().decodeToString())
    }
}