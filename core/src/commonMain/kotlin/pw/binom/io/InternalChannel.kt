package pw.binom.io

import pw.binom.ByteBuffer

class InternalChannel(val readBuffer:ByteBuffer, val writeBuffer:ByteBuffer):Channel {
    override fun read(dest: ByteBuffer): Int {
        val p = readBuffer.position
        readBuffer.flip()
        val r = readBuffer.read(dest)
        readBuffer.compact()
        readBuffer.position = p - r
        return r
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun write(data: ByteBuffer): Int {
        return writeBuffer.write(data)
    }

    override fun flush() {
        TODO("Not yet implemented")
    }
}