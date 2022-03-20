package pw.binom.io

import kotlinx.coroutines.test.runTest
import pw.binom.asyncOutput
import pw.binom.io.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BufferedAsciiWriterTest {
    @Test
    fun test() = runTest {
        val out = ByteArrayOutput()

        out.asyncOutput().bufferedAsciiWriter(closeParent = false).use {
            it.append("Anton")
        }
        assertEquals("Anton", out.toByteArray().decodeToString())
    }

    @Test
    fun test2() = runTest {
        val out = ByteArrayOutput()

        out.asyncOutput().bufferedAsciiWriter(closeParent = false).use {
            it.append("A")
        }
        assertEquals("A", out.toByteArray().decodeToString())
    }
}
