package pw.binom.io.http

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*

internal const val CR = 0x0D.toByte()
internal const val LF = 0x0A.toByte()

/**
 * Implements Async Http Chunked Transport Input
 *
 * @param stream real output stream
 * @param closeStream flag for auto close [stream] when this stream will close
 */
open class AsyncChunkedInput(val stream: AsyncInput, val closeStream: Boolean = false) : AsyncHttpInput {

  private suspend fun AsyncInput.readLineCRLF(): String {
    val sb = StringBuilder()
    while (true) {
      val r = read()
      if (r == CR) {
        if (read() != LF) {
          throw IllegalStateException("Invalid end of line")
        }
        return sb.toString()
      }

      if (r == 13.toByte()) {
        continue
      }
      sb.append(r.toInt().toChar())
    }
  }

  private suspend fun AsyncInput.read(): Byte {
    staticData.reset(0, 1)
    readFully(staticData)
    return staticData[0]
  }

  private val staticData = ByteBuffer(2)
  override val isEof: Boolean
    get() = closed.getValue() || eof

  override val available: Int
    get() = if (eof) 0 else -1

  private var chunkedSize: ULong = 0u
  private var readed = 0uL
  private var eof = false
  private var closed = AtomicBoolean(false)

  private suspend fun readChunkSize() {
    if (eof) {
      return
    }
    val chunkedSize = stream.readLineCRLF()
    if (chunkedSize.isEmpty()) {
      eof = true
      return
    }
    this.chunkedSize = chunkedSize.toULongOrNull(16) ?: throw IOException("Invalid Chunk Size: \"$chunkedSize\"")
    readed = 0uL

    if (this.chunkedSize == 0uL) {
      readCRLF()
      eof = true
      return
    }
  }

  private suspend fun readCRLF() {
    staticData.reset(position = 0, length = 2)
    stream.readFully(staticData)
    val b1 = staticData[0]
    val b2 = staticData[1]
    if (b1 != CR || b2 != LF) {
      throw IOException("Invalid end of chunk")
    }
  }

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    ensureOpen()
    while (true) {
      if (eof) {
        return DataTransferSize.EMPTY
      }
      // check chunk not exist
      if (chunkedSize == 0uL) {
        readChunkSize()
        if (eof) {
          return DataTransferSize.EMPTY
        }
      }

      // check chunk is finished
      val remainingChunk = chunkedSize - readed
      if (remainingChunk == 0uL) {
        chunkedSize = 0uL
        readCRLF()
        continue
      }

      val r = minOf(remainingChunk, dest.remaining.toULong())
//            val oldLimit = dest.limit
      dest.limit = dest.position + r.toInt()
      val b = stream.read(dest)
//            dest.limit = oldLimit
      if (b.isAvailable) {
        readed += b.length.toULong()
      }
//      if (chunkedSize!! - readed == 0uL) {
//        chunkedSize = null
//      }
//      if (chunkedSize - readed == 0uL) {
//        staticData.reset(position = 0, length = 2)
//        stream.readFully(staticData)
//        val b1 = staticData[0]
//        val b2 = staticData[1]
//        if (b1 != CR || b2 != LF) {
//          throw IOException("Invalid end of chunk")
//        }
//        readChunkSize()
//      }
      return b
    }
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(expected = false, new = true)) {
      return
    }
    if (!eof) {
      skipAll()
    }
    staticData.close()
    if (closeStream) {
      stream.asyncClose()
    }
  }

  protected fun ensureOpen() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }

  override suspend fun skipAll(buffer: ByteBuffer) {
    if (closed.getValue()) {
      return
    }
    super.skipAll(buffer)
  }
}
