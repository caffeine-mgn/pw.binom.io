package pw.binom.compression.zlib

import pw.binom.ByteBuffer

object TestData {
    val COMPRESSED = byteArrayOf(120, -100, -29, -30, -62, 7, 0, 18, 72, 1, 45)
    val SOURCE_DATA = ByteBuffer.alloc(30).also {
        repeat(it.capacity) { _ ->
            it.put(10)
        }
    }

}