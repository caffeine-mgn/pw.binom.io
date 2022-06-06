package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.base63.toJavaBigInteger
import org.bouncycastle.math.ec.ECCurve as BCECCurve

actual class ECCurve(val native: BCECCurve) {
    actual fun decodePoint(data: ByteArray): EcPoint = EcPoint(native.decodePoint(data), this)

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

    actual companion object {
        actual fun generate(params: X9ECParameters): ECCurve = ECCurve(params.native.curve)
    }

    actual val fieldSizeInBits: Int
        get() = native.fieldSize
    actual val fieldSizeInBytes: Int
        get() = (fieldSizeInBits + 7) / 8
}
