package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.bouncycastle.math.ec.ECPoint as BCECPoint

actual class EcPoint(val native: BCECPoint, actual val curve: ECCurve) {
    actual fun multiply(k: BigInteger): EcPoint = EcPoint(native.multiply(k.toJBigInteger()), curve)

    actual val x: BigInteger
        get() = native.xCoord.toBigInteger().toBigInteger()
    actual val y: BigInteger
        get() = native.yCoord.toBigInteger().toBigInteger()

    actual fun getEncoded(compressed: Boolean) = native.getEncoded(compressed)
}
