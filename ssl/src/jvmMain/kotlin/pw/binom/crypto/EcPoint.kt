package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.base63.toJavaBigInteger
import org.bouncycastle.math.ec.ECPoint as BCECPoint

actual class EcPoint(val native: BCECPoint, actual val curve: ECCurve) {
    actual val x: BigInteger
        get() = native.xCoord.toBigInteger().toBigInteger()
    actual val y: BigInteger
        get() = native.yCoord.toBigInteger().toBigInteger()

    actual fun getEncoded(compressed: Boolean) = native.getEncoded(compressed)
    actual val isInfinity: Boolean
        get() = native.isInfinity

    actual fun add(k: EcPoint): EcPoint = EcPoint(native.add(k.native), curve)
    actual fun multiply(k: BigInteger): EcPoint = EcPoint(native.multiply(k.toJavaBigInteger()), curve)
}
