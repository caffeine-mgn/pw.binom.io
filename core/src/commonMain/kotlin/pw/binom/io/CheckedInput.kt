package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.Input

class CheckedInput(val stream: Input, val cksum: CRC32Basic) : Input {

    override fun close() {
        stream.close()
    }

    override fun read(dest: ByteBuffer): Int {
        val ll = stream.read(dest)
        if (ll != -1) {
            dest.flip()
            cksum.update(dest)
        }
        return ll
    }
}
