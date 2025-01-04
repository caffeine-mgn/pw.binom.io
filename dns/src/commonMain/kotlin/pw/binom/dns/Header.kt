package pw.binom.dns

import pw.binom.io.*
import pw.binom.readShort
import pw.binom.writeShort

data class Header(
  /**
   * identification number
   */
  var id: Short = 0,

  /**
   * query/response flag
   */
  var qr: Boolean = false,

  /**
   * purpose of message
   */
  var opcode: Opcode = Opcode.QUERY,

  /**
   * authoritive answer
   *
   * Данное поле имеет смысл только в DNS-ответах от сервера и сообщает о том, является ли ответ авторитетным либо нет.
   */
  var aa: Boolean = false,


  /**
   * truncated message
   *
   * Данный флаг устанавливается в пакете ответе в том случае если сервер не смог поместить всю необходимую информацию в пакет из-за существующих ограничений.
   */
  var tc: Boolean = false,

  /**
   * recursion desired
   */
  var rd: Boolean = false,


  /**
   * Recursion available
   *
   * отправляется только в ответах, и сообщает о том, что сервер поддерживает рекурсию
   */
  var ra: Boolean = false,

  /**
   * its z! reserved
   */
  var z: Boolean = false,

  /**
   * authenticated data
   */
  var ad: Boolean = false,

  /**
   * checking disabled
   */
  var cd: Boolean = false,

  /**
   * response code
   */
  var rcode: Rcode = Rcode.NOERROR,
) {

  companion object {
    const val SIZE_BYTES = Short.SIZE_BYTES * 2
  }

  private fun setFlags(value: Short) {
    Flags.readFlags(
      value = value,
      qr = { qr = it },
      aa = { aa = it },
      tc = { tc = it },
      rd = { rd = it },
      ra = { ra = it },
      z = { z = it },
      ad = { ad = it },
      cd = { cd = it },
      opcode = { opcode = Opcode(it) },
      rcode = { rcode = Rcode(it) },
    )
  }

  fun read(buffer: ByteBuffer) {
    id = buffer.readShort()
    setFlags(buffer.readShort())
  }

  suspend fun read(input: AsyncInput, buffer: ByteBuffer) {
    id = input.readShort(buffer)
    setFlags(input.readShort(buffer))
  }

  fun read(input: Input, buffer: ByteBuffer) {
    id = input.readShort(buffer)
    setFlags(input.readShort(buffer))
  }

  private fun getFlags() =
    Flags.writeFlags(
      qr = qr,
      aa = aa,
      tc = tc,
      rd = rd,
      ra = ra,
      z = z,
      ad = ad,
      cd = cd,
      opcode = opcode.raw,
      rcode = rcode.raw,
    )

//  private fun getFlags() = BitArray32().update(0 + Short.SIZE_BITS, qr).updateByte4(1 + Short.SIZE_BITS, opcode.raw)
//    .update(5 + Short.SIZE_BITS, aa).update(6 + Short.SIZE_BITS, tc).update(7 + Short.SIZE_BITS, rd)
//    .update(8 + Short.SIZE_BITS, ra).update(9 + Short.SIZE_BITS, z).update(10 + Short.SIZE_BITS, ad)
//    .update(11 + Short.SIZE_BITS, cd).updateByte4(12 + Short.SIZE_BITS, rcode.raw).toInt().let { it and 0xFFF }
//    .toShort()

  suspend fun write(output: AsyncOutput, buffer: ByteBuffer) {
    output.writeShort(value = id, buffer = buffer)
    output.writeShort(value = getFlags(), buffer = buffer)
  }

  fun write(output: Output, buffer: ByteBuffer) {
    output.writeShort(value = id, buffer = buffer)
    output.writeShort(value = getFlags(), buffer = buffer)
  }

  fun write(buffer: ByteBuffer) {
    buffer.writeShort(id)
    buffer.writeShort(getFlags())
  }
}
