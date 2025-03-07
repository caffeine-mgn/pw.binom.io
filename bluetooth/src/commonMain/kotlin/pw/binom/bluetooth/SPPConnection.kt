package pw.binom.bluetooth

import pw.binom.io.Channel
import pw.binom.io.Closeable

expect class SPPConnection : Channel {
//  fun write(data: ByteArray, offset: Int, size: Int = data.size - offset): Int
//  fun read(data: ByteArray, offset: Int, size: Int = data.size - offset): Int
  override fun close()
}
