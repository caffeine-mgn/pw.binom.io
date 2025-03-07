package pw.binom.bluetooth

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.Closeable
import pw.binom.io.DataTransferSize

expect class SPPConnection : Channel {
//  fun write(data: ByteArray, offset: Int, size: Int = data.size - offset): Int
//  fun read(data: ByteArray, offset: Int, size: Int = data.size - offset): Int
  override fun close()
  override fun read(dest: ByteBuffer): DataTransferSize
  override fun write(data: ByteBuffer): DataTransferSize
  override fun flush()
}
