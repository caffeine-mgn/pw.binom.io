package pw.binom.io

import pw.binom.ByteBuffer
import java.security.MessageDigest as JMessageDigest

abstract class AbstractJavaMessageDigest : MessageDigest {

    protected abstract val messageDigest: JMessageDigest

    override fun init() {
        messageDigest.reset()
    }

    override fun update(byte: Byte) {
        messageDigest.update(byte)
    }

    override fun update(buffer: ByteBuffer) {
        val md = messageDigest
        md.update(buffer.native)
    }

    override fun update(byte: ByteArray) {
        val md = messageDigest
        md.update(byte)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        val md = messageDigest
        md.update(input, offset, len)
    }

    override fun finish(): ByteArray {
        val md = messageDigest
        return md.digest()
    }
}