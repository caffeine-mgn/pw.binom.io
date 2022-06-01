package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.base63.toJavaBigInteger
import org.bouncycastle.math.ec.ECCurve as BCECCurve

actual class ECCurve(val native: BCECCurve) {
    actual fun decodePoint(data: ByteArray, yBit: Boolean): EcPoint {
        TODO("Not yet implemented")
    }

    actual fun createPoint(
        x: BigInteger,
        y: BigInteger
    ): EcPoint {
        val ptr = native.createPoint(x.toJavaBigInteger(), y.toJavaBigInteger())
        return EcPoint(
            native = ptr,
            curve = this,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ECCurve

        if (native != other.native) return false

        return true
    }

    override fun hashCode(): Int {
        return native.hashCode()
    }
}
