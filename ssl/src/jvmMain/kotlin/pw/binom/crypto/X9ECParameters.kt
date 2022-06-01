package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import org.bouncycastle.asn1.x9.X9ECParameters as BCX9ECParameters
import java.math.BigInteger as JBigInteger

actual class X9ECParameters(val native: BCX9ECParameters) {
    actual val seed: ByteArray?
        get() = if (native.hasSeed()) native.seed else null
    actual val n: BigInteger
        get() = native.n.toBigInteger()
    actual val h: BigInteger
        get() = native.h.toBigInteger()
    actual val curve: ECCurve by lazy { ECCurve(native.curve) }
    actual val g: EcPoint by lazy { EcPoint(native.g, curve) }
}

fun JBigInteger.toBigInteger() = BigInteger.fromByteArray(toByteArray(), signum())

fun BigInteger.Companion.fromByteArray(
    source: ByteArray,
    sign: Int
): BigInteger {
    val signValue = when (sign) {
        -1 -> Sign.NEGATIVE
        0 -> Sign.ZERO
        1 -> Sign.POSITIVE
        else -> throw IllegalArgumentException("Invalid sign $sign")
    }
    return BigInteger.fromByteArray(source, signValue)
}

fun BigInteger.toJBigInteger() = JBigInteger(signum(), toByteArray())
