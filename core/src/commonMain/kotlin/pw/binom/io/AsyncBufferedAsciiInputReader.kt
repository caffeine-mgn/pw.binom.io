package pw.binom.io

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.InternalLog
import pw.binom.atomic.AtomicBoolean
import pw.binom.fromBytes

class AsyncBufferedAsciiInputReader private constructor(
  val stream: AsyncInput,
  val closeParent: Boolean = true,
) : AsyncReader, BufferedAsyncInput {
  private var buffer: ByteBuffer = ByteBuffer(0)

  private val logger = InternalLog.file("AsyncBufferedAsciiInputReader")


  constructor(
    stream: AsyncInput,
    pool: ByteBufferPool,
    closeParent: Boolean = true,
  ) : this(
    stream = stream,
    closeParent = closeParent,
  ) {
    buffer.close()
    buffer = pool.borrow(this).empty()
  }

  constructor(
    stream: AsyncInput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    closeParent: Boolean = true,
  ) : this(
    stream = stream,

    closeParent = closeParent,
  ) {
    buffer.close()
    buffer = ByteBuffer(bufferSize).empty()
  }

  init {
    buffer.empty()
  }

  private var eof = false
  private var closed = AtomicBoolean(false)

  fun reset() {
    ensureOpen()
    buffer.empty()
  }

  private fun ensureOpen() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }

//    private val buffer = ByteBuffer(bufferSize).empty()

  override val available: Int
    get() = if (closed.getValue()) 0 else if (buffer.remaining > 0) buffer.remaining else -1

  init {
//        println("AsyncBufferedAsciiInputReader:: after construct. ${buffer.position}, limit: ${buffer.limit}, byteBuffer: ByteBuffer@${buffer.hashCode()}")
  }

  override val inputBufferSize: Int
    get() = buffer.capacity

  override fun toString(): String = "AsyncBufferedAsciiInputReader(stream=$stream)"

  private suspend fun full(minSize: Int = 1) {
    if (eof) {
      return
    }
    if (buffer.remaining >= minSize) {
      return
    }
//            println("AsyncBufferedAsciiInputReader::full #1. position: ${buffer.position}, limit: ${buffer.limit}, byteBuffer: ByteBuffer@${buffer.hashCode()}")
    try {
      buffer.compact()

      logger.info{ "Reading from stream ${buffer.remaining} bytes... Stream: $stream" }
      val len = stream.read(buffer)
      if (len.isNotAvailable) {
        logger.info { "Got from stream $len. mark stream as EOF. Stream: $stream" }
        eof = true
      } else {
        logger.info { "Was read from stream $len bytes. Stream: $stream" }
      }
      buffer.flip()
    } catch (e: Throwable) {
      logger.info { "Can't read ${buffer.remaining} bytes from stream. Stream: $stream, e: $e" }
      buffer.empty()
      throw e
    }
  }

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    ensureOpen()
    full()
    return buffer.read(dest)
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    buffer.close()
    if (closeParent) {
      logger.info { "Closing stream. Eof: $eof, Stream: $stream" }
      stream.asyncClose()
    } else {
      logger.info { "Closing without close parent stream. Eof: $eof, Stream: $stream" }
    }
  }

  override suspend fun readChar(): Char? {
    ensureOpen()
    full()
    if (buffer.remaining <= 0) {
      return null
    }
    return buffer.getByte().toInt().toChar()
  }

  override suspend fun read(dest: CharArray, offset: Int, length: Int): DataTransferSize {
    ensureOpen()
    full()
    val len = minOf(minOf(dest.size - offset, length), buffer.remaining)
    for (i in offset until offset + len) {
      dest[i] = buffer.getByte().toInt().toChar()
    }
    return DataTransferSize.ofSize(len)
  }

  override suspend fun readByte(): Byte {
    full(Byte.SIZE_BYTES)
    if (buffer.remaining < Byte.SIZE_BYTES) {
      throw EOFException()
    }
    return buffer.getByte()
  }

  override suspend fun readShort(): Short {
    full(Short.SIZE_BYTES)
    if (buffer.remaining < Short.SIZE_BYTES) {
      throw EOFException()
    }
    return Short.fromBytes(buffer)
  }

  override suspend fun readInt(): Int {
    full(Int.SIZE_BYTES)
    if (buffer.remaining < Int.SIZE_BYTES) {
      throw EOFException()
    }
    return Int.fromBytes(buffer)
  }

  override suspend fun readLong(): Long {
    full(Long.SIZE_BYTES)
    if (buffer.remaining < Long.SIZE_BYTES) {
      throw EOFException()
    }
    return Long.fromBytes(buffer)
  }

  override suspend fun read(dest: ByteArray, offset: Int, length: Int): Int {
    ensureOpen()
    full()
    val len = minOf(minOf(dest.size - offset, length), buffer.remaining)
    buffer.readInto(
      dest = dest,
      offset = offset,
      length = len,
    )
    return len
  }

  override suspend fun readFully(dest: ByteArray, offset: Int, length: Int): Int {
    ensureOpen()
    var readed = 0
    while (true) {
      val r = read(dest, offset + readed, length - readed)
      readed += r
      if (readed == length) {
        return length
      }
      if (r == 0) {
        throw EOFException()
      }
    }
  }

  suspend fun readUntil(stopByte: Byte, exclude: Boolean, dest: AsyncOutput) =
    readUntil(
      condition = { it == stopByte },
      exclude = exclude,
      dest = dest
    )

  suspend fun readUntil(condition: (Byte) -> Boolean, exclude: Boolean, dest: AsyncOutput): DataTransferSize {
    ensureOpen()
    var readSize = 0
    while (!eof) {
      full(1)
//      if (eof && !buffer.hasRemaining) {
//        return DataTransferSize.EMPTY
//      }
      val index = buffer.indexOfFirst(condition)
      logger.info { "readUntil index=$index buffer.remaining=${buffer.remaining}" }
      if (index == -1) {
        readSize += dest.writeFully(buffer)
      } else {
        val l = buffer.limit
        buffer.limit = if (exclude) index else index + 1
        readSize += dest.writeFully(buffer)
        buffer.limit = l
        if (exclude) {
          buffer.position++
          readSize++
        }
        break
      }
    }
    logger.info { "readed = $readSize" }
    return DataTransferSize.ofSize(readSize)
  }

  //    suspend fun readUntil(char: Char): String? {
//        val byte = char.code.toByte()
//        val sb = StringBuilder()
//        try {
//            ensureOpen()
//            while (true) {
//                full(1)
//                val index = buffer.indexOfFirst { it == byte }
//                if (index == -1) {
//                    buffer.forEach {
//                        sb.append(it.toInt().toChar())
//                    }
//                } else {
//                    val l = buffer.limit
//                    val limit = if (true) index else index + 1
//                    buffer.forEach(buffer.position..limit) {
//                        sb.append(it.toInt().toChar())
//                    }
//                    buffer.limit = l
//                    if (true) {
//                        buffer.position++
//                    }
//                    break
//                }
//            }
//            if (sb.isEmpty()) {
//                return null
//            }
//            return sb.toString()
//        } catch (e: Throwable) {
//            println("AsyncBufferedAsciiInputReader2 BinomError, byteBuffer: ByteBuffer@${buffer.hashCode()}")
//            e.printStackTrace()
//            throw e
//        }
//    }
  suspend fun readUntil(char: Char): String? = readUntil {
    it == char
  }

  suspend fun readUntil(condition: (Char) -> Boolean): String? {
    ensureOpen()
    val outputString = AsyncOutputAsciStringAppender()
    val r = readUntil(condition = { condition(it.toInt().toChar()) }, exclude = true, dest = outputString)
    if (r.isNotAvailable) {
      return null
    }

    return outputString.toString().trimEnd()
  }

  override suspend fun readln(): String? {
    val result = readUntil { it == 10.toChar() }?.removeSuffix("\r")
    return result
  }
}

fun AsyncInput.bufferedAsciiReader(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) =
  AsyncBufferedAsciiInputReader(
    stream = this,
    bufferSize = bufferSize,
    closeParent = closeParent,
  )

fun AsyncInput.bufferedAsciiReader(pool: ByteBufferPool, closeParent: Boolean = true) =
  AsyncBufferedAsciiInputReader(
    stream = this,
    pool = pool,
    closeParent = closeParent,
  )
