package pw.binom.io

import pw.binom.ByteBuffer
import java.security.MessageDigest as JMessageDigest

actual class MD5 : MessageDigest {

    private var md = JMessageDigest.getInstance("MD5")

    override fun init() {
        md.reset()
    }

    override fun update(byte: Byte) {
        md.update(byte)
    }

    override fun update(buffer: ByteBuffer) {
        val md = md
        md.update(buffer.native)
    }

    override fun update(byte: ByteArray) {
        val md = md
        md.update(byte)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        val md = md
        md.update(input, offset, len)
    }

    override fun finish(): ByteArray {
        val md = md
        return md.digest()
    }

}