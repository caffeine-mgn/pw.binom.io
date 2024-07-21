package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import pw.binom.io.empty

open class InflateInput(
  val stream: Input,
  bufferSize: Int = 512,
  wrap: Boolean = false,
  val closeStream: Boolean = false,
) : Input {
  private val buf2 = ByteBuffer(bufferSize).empty()
  private val inflater = Inflater(wrap)
  protected var usesDefaultInflater = true
  private var first = true

//    override fun skip(length: Long): Long {
//        var l = length
//        while (l > 0) {
//            tmpBuf.reset(0, minOf(tmpBuf.capacity, l.toInt()))
//            l -= read(tmpBuf)
//        }
//        return length
//    }

  override fun read(dest: ByteBuffer): DataTransferSize {
    val l = dest.remaining
    while (true) {
      full2()
      if (buf2.remaining == 0 || dest.remaining == 0) {
        break
      }
      val r = inflater.inflate(buf2, dest)
      if (r == 0) {
        break
      }
    }
    return DataTransferSize.ofSize(l - dest.remaining)
  }

  protected fun full2() {
    if (buf2.remaining > 0) {
      return
    }
    buf2.clear()
    stream.read(buf2)
    buf2.flip()
  }

//    protected fun full() {
//        if (!first && cursor.availIn > 0)
//            return
//
//        cursor.inputOffset = 0
//        cursor.inputLength = stream.read(buf, 0, buf.size)
//        cursor.inputLength = maxOf(0, cursor.inputLength)
//        first = false
//    }
//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        cursor.outputLength = length
//        cursor.outputOffset = offset
//        while (true) {
//            full()
//            if (cursor.availIn == 0 || cursor.availOut == 0)
//                break
//            val r = inflater.inflate(cursor, buf, data)
//            if (r == 0)
//                break
//        }
//        return length - cursor.outputLength
//    }

  override fun close() {
    if (usesDefaultInflater) {
      try {
        inflater.end()
      } catch (e: Throwable) {
        // Do nothing
      }
    }
    inflater.closeAnyway()
    buf2.close()
    if (closeStream) {
      stream.close()
    }
  }
}
