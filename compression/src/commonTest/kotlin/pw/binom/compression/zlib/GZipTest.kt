package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.asyncOutput
import pw.binom.clone
import kotlin.test.Test
import kotlin.test.assertEquals

class GZipTest {




}
/*

fun Output.noCloseWrapper() = object : Output {
    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
            this@noCloseWrapper.write(data, offset, length)

    override fun flush() {
        this@noCloseWrapper.flush()
    }

    override fun close() {
    }

}

class GZipTest {

    @Ignore
    @Test
    fun testSync() {
        val data = ByteDataBuffer.alloc(200) { it.toByte() }
        val compressed = ByteArrayOutput()
        GZIPOutput(compressed.noCloseWrapper(), 9).use {
            val r = it.write(data)
            if (r != data.size)
                TODO()
        }

        println("size: ${compressed.data.size}")
        assertEquals(223, compressed.size)
        compressed.trimToSize()

        File("test-zip.gz").channel(AccessType.WRITE, AccessType.CREATE).use {
            it.write(compressed.data)
        }

        println("Compressed:")
        compressed.data.forEachIndexed { index, byte ->
            println("$index -> $byte")
        }

        val uncompressed = ByteArrayOutput()
        GZIPInput(ByteArrayInput(compressed.data)).use {
            it.copyTo(uncompressed)
        }

        uncompressed.trimToSize()

        println("Uncompressed:")
        uncompressed.data.forEachIndexed { index, byte ->
            println("$index -> $byte")
        }

        uncompressed.data.let {
            assertEquals(data.size, it.size)
            it.forEachIndexed { index, byte ->
                assertEquals(data[index], byte)
            }
        }

    }

    @Test
    fun testAsync2() {
        val bufPool = ByteDataBufferPool()
        async {
            val data = ByteDataBuffer.alloc(1024 * 16)
            Random.nextBytes(data)
            val compressed = ByteArrayOutput()
            AsyncGZIPOutput(compressed.noCloseWrapper().asyncOutput(), 9).use {
                it.write(data)
                it.flush()
            }

            compressed.trimToSize()
            val uncompressed = ByteArrayOutput()
            AsyncGZIPInput(ByteArrayInput(compressed.data).asyncInput()).use {
                it.copyTo(uncompressed, bufPool)
            }

            uncompressed.data.let {
                assertEquals(data.size, it.size)
                it.forEachIndexed { index, byte ->
                    assertEquals(data[index], byte)
                }
            }
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
}*/
