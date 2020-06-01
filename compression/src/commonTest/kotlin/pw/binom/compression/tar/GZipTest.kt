package pw.binom.compression.tar

import pw.binom.async
import pw.binom.compression.zlib.AsyncGZIPInputStream
import pw.binom.compression.zlib.AsyncGZIPOutputStream
import pw.binom.compression.zlib.GZIPInputStream
import pw.binom.compression.zlib.GZIPOutputStream
import pw.binom.io.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class GZipTest {

    @Test
    fun testSync() {
        val data = ByteArray(1024 * 16)
        Random.nextBytes(data)
        val compressed = ByteArrayOutputStream()
        GZIPOutputStream(compressed.noCloseWrapper(), 9).use {
            it.write(data)
            it.flush()
        }

        val uncompressed = ByteArrayOutputStream()
        GZIPInputStream(ByteArrayInputStream(compressed.toByteArray())).use {
            it.copyTo(uncompressed)
        }

        uncompressed.toByteArray().let {
            assertEquals(data.size, it.size)
            it.forEachIndexed { index, byte ->
                assertEquals(data[index], byte)
            }
        }

        @Test
        fun testAsync() {
            async {
                val data = ByteArray(1024 * 16)
                Random.nextBytes(data)
                val compressed = ByteArrayOutputStream()
                AsyncGZIPOutputStream(compressed.noCloseWrapper().asAsync(), 9).use {
                    it.write(data)
                    it.flush()
                }

                val uncompressed = ByteArrayOutputStream()
                AsyncGZIPInputStream(ByteArrayInputStream(compressed.toByteArray()).asAsync()).use {
                    it.copyTo(uncompressed)
                }

                uncompressed.toByteArray().let {
                    assertEquals(data.size, it.size)
                    it.forEachIndexed { index, byte ->
                        assertEquals(data[index], byte)
                    }
                }
            }
        }
    }
}