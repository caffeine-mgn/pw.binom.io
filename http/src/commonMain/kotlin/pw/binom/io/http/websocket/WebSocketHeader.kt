package pw.binom.io.http.websocket

import pw.binom.*
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.random.Random

class WebSocketHeader {
  var opcode: Opcode = Opcode(0)
  var length = 0L
  var maskFlag = false
  var mask = 0
  var finishFlag = false

  companion object {
    suspend fun read(input: AsyncInput, dest: WebSocketHeader) {
      ByteBuffer(8).use { buf ->
        val first = input.readByte(buf)
        val second = input.readByte(buf)

        dest.finishFlag = first.toInt() and 0b10000000 != 0
        dest.opcode = Opcode(first and 0b1111)

        dest.length = (second and 0b1111111.toByte()).let {
          when (it) {
            126.toByte() -> {
              input.readShort(buf).toUShort().toLong()
            }

            127.toByte() -> {
              input.readLong(buf)
            }

            else -> {
              it.toLong()
            }
          }
        }
        dest.maskFlag = second and 0b10000000.toByte() != 0.toByte()

        if (dest.maskFlag) {
          dest.mask = input.readInt(buf)
        }
      }
    }

    suspend fun write(
      output: AsyncOutput,
      opcode: Opcode = Opcode.BINARY,
      length: Long,
      maskFlag: Boolean = false,
      mask: Int = Random.nextInt(),
      finishFlag: Boolean = false,
    ) {
      ByteBuffer(8).use { buf ->
        var value = opcode.raw and 0b1111
        if (finishFlag) {
          value = value or 0b10000000.toByte()
        }
        output.writeByte(value = value, buffer = buf)

        value = if (maskFlag) {
          0b10000000.toByte()
        } else {
          0b00000000.toByte()
        }
        when {
          length > Short.MAX_VALUE -> {
            output.writeByte(value = 127.toByte() or value, buffer = buf)
            output.writeLong(value = length, buffer = buf)
          }

          length >= 126L -> {
            output.writeByte(value = 126.toByte() or value, buffer = buf)
            output.writeShort(value = length.toShort(), buffer = buf)
          }

          else -> output.writeByte(value = length.toByte() or value, buffer = buf)
        }
        if (maskFlag) {
          output.writeInt(value = mask, buffer = buf)
        }
      }
    }

    suspend fun write(output: AsyncOutput, src: WebSocketHeader) {
      write(
        output = output,
        opcode = src.opcode,
        length = src.length,
        maskFlag = src.maskFlag,
        mask = src.mask,
        finishFlag = src.finishFlag,
      )
    }
  }

  override fun toString(): String =
    "WebSocketHeader(opcode=$opcode, length=$length, maskFlag=$maskFlag, mask=${
      mask.toUInt().toString(2)
    }, finishFlag=$finishFlag)"
}
