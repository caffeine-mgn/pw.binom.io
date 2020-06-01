package pw.binom.compression.tar

import pw.binom.compression.zlib.DeflaterOutputStream
import pw.binom.compression.zlib.GZIPInputStream
import pw.binom.compression.zlib.GZIPOutputStream
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.outputStream
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun octTest() {
        val original = "0100777".toUInt()
        val d = ByteArray(15)
        original.toOct(d, 0, 15)
        assertEquals(original, d.oct2ToUInt(0, 15))
    }
}