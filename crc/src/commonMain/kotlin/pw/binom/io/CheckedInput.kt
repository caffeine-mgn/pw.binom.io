package pw.binom.io

class CheckedInput(val stream: Input, val cksum: MessageDigest) : Input {

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
