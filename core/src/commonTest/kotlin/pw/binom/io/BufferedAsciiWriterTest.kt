package pw.binom.io

import pw.binom.async2
import pw.binom.asyncOutput
import kotlin.test.Test
import pw.binom.io.*
import kotlin.test.assertEquals

class BufferedAsciiWriterTest {
    @Test
    fun test() {
        val out = ByteArrayOutput()

        async2 {
            out.asyncOutput().bufferedAsciiWriter(closeParent = false).use {
                it.append("Anton")
            }
        }
        assertEquals("Anton", out.toByteArray().decodeToString())
    }

    @Test
    fun test2() {
        val out = ByteArrayOutput()

        async2 {
            out.asyncOutput().bufferedAsciiWriter(closeParent = false).use {
                it.append("A")
            }
        }
        assertEquals("A", out.toByteArray().decodeToString())
    }
}