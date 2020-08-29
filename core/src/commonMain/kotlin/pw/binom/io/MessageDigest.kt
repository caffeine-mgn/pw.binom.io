package pw.binom.io

import pw.binom.ByteBuffer

interface MessageDigest {
    fun init()
    fun update(byte: Byte)
    fun update(buffer: ByteBuffer) {
        while (buffer.remaining > 0) {
            update(buffer.get())
        }
    }

    fun finish(): ByteArray
}