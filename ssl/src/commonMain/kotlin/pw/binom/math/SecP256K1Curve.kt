package pw.binom.math

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

class SecP256K1Curve {
    companion object {
        val q = BigInteger.fromByteArray(
            sign = Sign.POSITIVE,
            source =
            byteArrayOf(
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -1, -1, -4, 47,
            )
        )
    }
}
