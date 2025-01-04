package pw.binom.dns

import pw.binom.bitarray.BitArray16
import pw.binom.bitarray.toBitset
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
value class Flags(val raw: Short) {
  companion object {
    fun writeFlags(
      qr: Boolean,
      aa: Boolean,
      tc: Boolean,
      rd: Boolean,
      ra: Boolean,
      z: Boolean,
      ad: Boolean,
      cd: Boolean,
      opcode: Byte,
      rcode: Byte,
    ): Short {
      var bitset = BitArray16()
      bitset = bitset.update(0, qr)
      bitset = bitset.updateByte4(1, opcode)
      bitset = bitset.update(5, aa)
      bitset = bitset.update(6, tc)
      bitset = bitset.update(7, rd)
      bitset = bitset.update(8, ra)
      bitset = bitset.update(9, z)
      bitset = bitset.update(10, ad)
      bitset = bitset.update(11, cd)
      bitset = bitset.updateByte4(12, rcode)
      return bitset.toShort()
    }

    @OptIn(ExperimentalContracts::class)
    inline fun readFlags(
      value: Short,
      qr: (Boolean) -> Unit = {},
      aa: (Boolean) -> Unit = {},
      tc: (Boolean) -> Unit = {},
      rd: (Boolean) -> Unit = {},
      ra: (Boolean) -> Unit = {},
      z: (Boolean) -> Unit = {},
      ad: (Boolean) -> Unit = {},
      cd: (Boolean) -> Unit = {},
      opcode: (Byte) -> Unit = {},
      rcode: (Byte) -> Unit = {},
    ) {
      contract {
        callsInPlace(qr, InvocationKind.EXACTLY_ONCE)
        callsInPlace(opcode, InvocationKind.EXACTLY_ONCE)
        callsInPlace(aa, InvocationKind.EXACTLY_ONCE)
        callsInPlace(tc, InvocationKind.EXACTLY_ONCE)
        callsInPlace(rd, InvocationKind.EXACTLY_ONCE)
        callsInPlace(ra, InvocationKind.EXACTLY_ONCE)
        callsInPlace(z, InvocationKind.EXACTLY_ONCE)
        callsInPlace(ad, InvocationKind.EXACTLY_ONCE)
        callsInPlace(cd, InvocationKind.EXACTLY_ONCE)
        callsInPlace(rcode, InvocationKind.EXACTLY_ONCE)
      }
      val bitset = value.toBitset()
      qr(bitset[0])
      opcode(bitset.getByte4(1)) // [1, 4]
      aa(bitset[5])
      tc(bitset[6])
      rd(bitset[7])
      ra(bitset[8])
      z(bitset[9])
      ad(bitset[10])
      cd(bitset[11])
      rcode(bitset.getByte4(12))
    }
  }

  constructor(
    /**
     * query/response flag
     */
    qr: Boolean = false,

    /**
     * purpose of message
     */
    opcode: Opcode = Opcode.QUERY,

    /**
     * authoritive answer
     *
     * Данное поле имеет смысл только в DNS-ответах от сервера и сообщает о том, является ли ответ авторитетным либо нет.
     */
    aa: Boolean = false,


    /**
     * truncated message
     *
     * Данный флаг устанавливается в пакете ответе в том случае если сервер не смог поместить всю необходимую информацию в пакет из-за существующих ограничений.
     */
    tc: Boolean = false,

    /**
     * recursion desired
     */
    rd: Boolean = false,


    /**
     * Recursion available
     *
     * отправляется только в ответах, и сообщает о том, что сервер поддерживает рекурсию
     */
    ra: Boolean = false,

    /**
     * its z! reserved
     */
    z: Boolean = false,

    /**
     * authenticated data
     */
    ad: Boolean = false,

    /**
     * checking disabled
     */
    cd: Boolean = false,

    /**
     * response code
     */
    rcode: Rcode = Rcode.NOERROR,
  ) : this(
    writeFlags(
      qr = qr,
      aa = aa,
      tc = tc,
      rd = rd,
      ra = ra,
      z = z,
      ad = ad,
      cd = cd,
      opcode = opcode.raw,
      rcode = rcode.raw
    )
  )

  fun copy(
    qr: Boolean = this.qr,

    /**
     * purpose of message
     */
    opcode: Opcode = this.opcode,

    /**
     * authoritive answer
     *
     * Данное поле имеет смысл только в DNS-ответах от сервера и сообщает о том, является ли ответ авторитетным либо нет.
     */
    aa: Boolean = this.aa,


    /**
     * truncated message
     *
     * Данный флаг устанавливается в пакете ответе в том случае если сервер не смог поместить всю необходимую информацию в пакет из-за существующих ограничений.
     */
    tc: Boolean = this.tc,

    /**
     * recursion desired
     */
    rd: Boolean = this.rd,


    /**
     * Recursion available
     *
     * отправляется только в ответах, и сообщает о том, что сервер поддерживает рекурсию
     */
    ra: Boolean = this.ra,

    /**
     * its z! reserved
     */
    z: Boolean = this.z,

    /**
     * authenticated data
     */
    ad: Boolean = this.ad,

    /**
     * checking disabled
     */
    cd: Boolean = this.cd,

    /**
     * response code
     */
    rcode: Rcode = this.rcode,
  ) = Flags(
    qr = qr,
    opcode = opcode,
    aa = aa,
    tc = tc,
    rd = rd,
    ra = ra,
    z = z,
    ad = ad,
    cd = cd,
    rcode = rcode
  )

  /**
   * query/response flag
   */
  val qr: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, qr = { r = it })
      return r
    }

  /**
   * purpose of message
   */
  val opcode: Opcode
    get() {
      var r: Opcode
      readFlags(value = raw, opcode = { r = Opcode(it) })
      return r
    }

  /**
   * authoritive answer
   *
   * Данное поле имеет смысл только в DNS-ответах от сервера и сообщает о том, является ли ответ авторитетным либо нет.
   */
  val aa: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, aa = { r = it })
      return r
    }


  /**
   * truncated message
   *
   * Данный флаг устанавливается в пакете ответе в том случае если сервер не смог поместить всю необходимую информацию в пакет из-за существующих ограничений.
   */
  val tc: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, tc = { r = it })
      return r
    }

  /**
   * recursion desired
   */
  val rd: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, rd = { r = it })
      return r
    }


  /**
   * Recursion available
   *
   * отправляется только в ответах, и сообщает о том, что сервер поддерживает рекурсию
   */
  val ra: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, ra = { r = it })
      return r
    }

  /**
   * its z! reserved
   */
  val z: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, z = { r = it })
      return r
    }

  /**
   * authenticated data
   */
  val ad: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, ad = { r = it })
      return r
    }

  /**
   * checking disabled
   */
  val cd: Boolean
    get() {
      var r: Boolean
      readFlags(value = raw, cd = { r = it })
      return r
    }

  /**
   * response code
   */
  val rcode: Rcode
    get() {
      var r: Rcode
      readFlags(value = raw, rcode = { r = Rcode(it) })
      return r
    }
}
