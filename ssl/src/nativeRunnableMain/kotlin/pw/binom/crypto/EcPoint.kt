package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.*
import pw.binom.security.SecurityException
import kotlin.native.internal.createCleaner

actual class EcPoint(actual val curve: ECCurve, val ptr: CPointer<EC_POINT>) {
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(ptr) { ptr ->
        EC_POINT_free(ptr)
    }

    actual fun multiply(k: BigInteger): EcPoint {
        val resultPoint = EC_POINT_new(curve.native) ?: throwError("EC_POINT_new fails")
        BigNumContext().use { ctx ->
            EC_POINT_mul(
                group = curve.native,
                r = resultPoint,
                n = null,
                q = ptr,
                m = k.toBigNum(ctx).ptr,
                ctx = ctx.ptr
            ).checkTrue("EC_POINT_mul fails") {
                EC_POINT_free(resultPoint)
            }
        }
        return EcPoint(curve, resultPoint)
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

    actual fun getEncoded(compressed: Boolean): ByteArray = memScoped {
        val bufPtr = allocPointerTo<UByteVar>()
        val c = if (compressed) POINT_CONVERSION_COMPRESSED else POINT_CONVERSION_UNCOMPRESSED
        val len = EC_POINT_point2buf(curve.native, ptr, c, bufPtr.ptr, null).toInt()
        if (len <= 0) {
            throw SecurityException("Can't encode ECPoint to ByteArray")
        }
        val buffer = bufPtr.value!!.readBytes(len)
        internal_OPENSSL_free(bufPtr.value)
        buffer
    }

    actual val isInfinity: Boolean
        get() = EC_POINT_is_at_infinity(curve.native, ptr) > 0

    fun copy() =
        EcPoint(curve, EC_POINT_dup(ptr, curve.native) ?: throwError("EC_POINT_dup fails"))

    actual fun add(k: EcPoint): EcPoint {
        val copy = copy()
        EC_POINT_add(curve.native, copy.ptr, ptr, k.ptr, null).checkTrue("EC_POINT_add fails")
        return copy
    }
}
