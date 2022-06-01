package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.CPointer
import platform.openssl.EC_POINT
import platform.openssl.EC_POINT_free
import platform.openssl.EC_POINT_get_affine_coordinates
import pw.binom.BigNum
import pw.binom.checkTrue
import pw.binom.toBigNum
import kotlin.native.internal.createCleaner

actual class EcPoint(actual val curve: ECCurve, val ptr: CPointer<EC_POINT>) {
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(ptr) { ptr ->
        EC_POINT_free(ptr)
    }

    actual fun multiply(k: BigInteger): EcPoint {
        TODO()
        val tmp = k.toBigNum()
//        EC_POINT_mul(
//            group = group,
//            r = ptr,
//        )

//        EC_POINT_mul(group, ptr, null, R, order, ctx)
//        ECDSA_sig_
//        EC_POINT_mul(
//            group,
//            ptr,
//            tmp.ptr,
//
//        )
        TODO("Not yet implemented")
    }

    actual val x: BigInteger
        get() =
            BigNum().use { num ->
                EC_POINT_get_affine_coordinates(
                    group = curve.native,
                    p = ptr,
                    x = num.ptr,
                    y = null,
                    ctx = null
                ).checkTrue("EC_POINT_get_affine_coordinates fails")
                num.toBigInt()
            }
    actual val y: BigInteger
        get() = BigNum().use { num ->
            EC_POINT_get_affine_coordinates(
                group = curve.native,
                p = ptr,
                x = null,
                y = num.ptr,
                ctx = null
            ).checkTrue("EC_POINT_get_affine_coordinates fails")
            num.toBigInt()
        }
}
