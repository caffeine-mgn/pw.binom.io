package pw.binom

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.posix.*
import pw.binom.io.*

// private val tmp1 = ByteBuffer(32)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual object Console {
  @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
  private class Out(val fd: Int) : Output {
    override fun close() {
    }

    override fun write(data: ByteBuffer): DataTransferSize {
      if (data.capacity == 0) {
        return DataTransferSize.EMPTY
      }
      return data.refTo(data.position) { data2 ->
        DataTransferSize.ofSize(write(fd, data2, data.remaining.convert()).convert())
      } ?: DataTransferSize.EMPTY
    }

    override fun flush() {
    }
  }

  actual val stdChannel: Output = Out(STDOUT_FILENO)
  actual val errChannel: Output = Out(STDERR_FILENO)

  actual val inChannel: Input =
    object : Input {
      override fun read(dest: ByteBuffer): DataTransferSize {
        if (dest.capacity == 0) {
          return DataTransferSize.EMPTY
        }
        return dest.refTo(dest.position) { dest2 ->
          write(STDIN_FILENO, dest2, dest.remaining.convert()).convert()
        } ?: DataTransferSize.EMPTY
      }

      override fun close() {
      }
    }

  private class StdOutput(val stream: CPointer<FILE>?) : Appendable {
    override fun append(value: Char): Appendable {
      fprintf(stream, value.toString())
      return this
    }

    override fun append(value: CharSequence?): Appendable {
      value ?: return this
      fprintf(stream, if (value is String) value else value.toString())
      return this
    }

    override fun append(
      value: CharSequence?,
      startIndex: Int,
      endIndex: Int,
    ): Appendable {
      value ?: return this
      fprintf(stream, value.substring(startIndex, endIndex))
      return this
    }
  }

  actual val std: Appendable = StdOutput(stdout)
  actual val err: Appendable = StdOutput(stderr)

  //    actual val input: Reader = ReaderUTF82(inChannel)
  actual val input: Reader =
    object : Reader {
      private fun readCharCode(): Int {
        val char = getc(stdin)
        when (char) {
          EOF -> -1
          EBADF -> throw IOException("The file pointer or descriptor is not valid.")
//            ECONVERT->throw IOException("A conversion error occurred.")
//            EGETANDPUT->throw IOException("An illegal read operation occurred after a write operation.")
//            EIOERROR->throw IOException("A non-recoverable I/O error occurred.")
//            EIORECERR->throw IOException("A recoverable I/O error occurred.")
        }
        return char
      }

      override fun readln(): String? = readlnOrNull()

      override fun read(): Char? {
        val b1 = readCharCode()
        if (b1 == -1) {
          return null
        }
        return if (b1 and 0x80 != 0 && (b1 and 0x40).inv() != 0) {
          val size = UTF8.getUtf8CharSize(b1.toByte()) - 1
          when (size) {
            1 -> return b1.toChar()
            2 -> {
              val b2 = readCharCode()
              if (b2 == -1) {
                return null
              }
              UTF8.utf8toUnicode(b1, b2)
            }

            3 -> {
              val b2 = readCharCode()
              if (b2 == -1) {
                return null
              }
              val b3 = readCharCode()
              if (b3 == -1) {
                return null
              }
              UTF8.utf8toUnicode(b1, b2, b3)
            }

            4 -> {
              val b2 = readCharCode()
              if (b2 == -1) {
                return null
              }
              val b3 = readCharCode()
              if (b3 == -1) {
                return null
              }
              val b4 = readCharCode()
              if (b4 == -1) {
                return null
              }
              UTF8.utf8toUnicode(b1, b2, b3, b4)
            }

            5 -> {
              val b2 = readCharCode()
              if (b2 == -1) {
                return null
              }
              val b3 = readCharCode()
              if (b3 == -1) {
                return null
              }
              val b4 = readCharCode()
              if (b4 == -1) {
                return null
              }
              val b5 = readCharCode()
              if (b5 == -1) {
                return null
              }
              UTF8.utf8toUnicode(b1, b2, b3, b4, b5)
            }

            6 -> {
              val b2 = readCharCode()
              if (b2 == -1) {
                return null
              }
              val b3 = readCharCode()
              if (b3 == -1) {
                return null
              }
              val b4 = readCharCode()
              if (b4 == -1) {
                return null
              }
              val b5 = readCharCode()
              if (b5 == -1) {
                return null
              }
              val b6 = readCharCode()
              if (b6 == -1) {
                return null
              }
              UTF8.utf8toUnicode(b1, b2, b3, b4, b5, b6)
            }

            else -> throw IllegalArgumentException("Unknown char")
          }
        } else {
          b1.toChar()
        }
      }

      override fun close() {
      }
    }
}
