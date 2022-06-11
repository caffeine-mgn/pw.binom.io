package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

class ECDSASignature(val r: BigInteger, val s: BigInteger) {
    fun toByteArray() = r.toByteArray() + s.toByteArray()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ECDSASignature

        if (r != other.r) return false
        if (s != other.s) return false

        return true
    }

    override fun hashCode(): Int {
        var result = r.hashCode()
        result = 31 * result + s.hashCode()
        return result
    }

    companion object {
        fun create(data: ByteArray): ECDSASignature {
            require(data.size == 64) { "Invalid signature size. Expected: 64,  Actual: ${data.size}" }
            val rdata = data.copyOfRange(0, 32)
            val sdata = data.copyOfRange(32, data.size)
            val r = BigInteger.fromByteArray(rdata, Sign.POSITIVE)
            val s = BigInteger.fromByteArray(sdata, Sign.POSITIVE)
            return ECDSASignature(
                r = r,
                s = s
            )
        }
    }
}
