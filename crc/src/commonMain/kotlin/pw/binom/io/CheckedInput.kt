package pw.binom.io

import pw.binom.security.MessageDigest

class CheckedInput(val stream: Input, val cksum: MessageDigest) : Input {

  override fun close() {
    stream.close()
  }

  override fun read(dest: ByteBuffer): DataTransferSize {
    val ll = stream.read(dest)
    if (ll.isAvailable) {
      dest.flip()
      cksum.update(dest)
    }
    return ll
  }
}
