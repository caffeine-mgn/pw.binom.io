package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.*
import kotlin.native.internal.createCleaner

actual class ECCurve(
    val native: CPointer<EC_GROUP>
) {

    actual companion object {
        actual fun generate(params: X9ECParameters): ECCurve =
            params.curve
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(native) {
        EC_GROUP_free(native)
    }

    actual fun createPoint(
        x: BigInteger,
        y: BigInteger,
    ): EcPoint {
        val ptr = EC_POINT_new(native) ?: throwError("EC_POINT_new fails")
        BigNumContext().use { ctx ->
            EC_POINT_set_affine_coordinates(
                native,
                ptr,
                x.toBigNum(ctx).ptr,
                y.toBigNum(ctx).ptr,
                ctx.ptr,
            ).checkTrue("EC_POINT_set_affine_coordinates fails")
        }
        return EcPoint(
            curve = this, ptr = ptr
        )
    }

    actual fun decodePoint(data: ByteArray): EcPoint {
        val point = EC_POINT_new(this.native) ?: throwError("EC_POINT_new fails")
        BigNumContext().use { ctx ->
            val bn = ctx.get()
            bn.setByteArray(data)
            val r = EC_POINT_bn2point(native, bn.ptr, point, ctx.ptr) ?: throwError("EC_POINT_bn2point fails") {
                EC_POINT_free(point)
            }
            check(point === r) { "Created point should be equals returned point" }
        }
        return EcPoint(curve = this, ptr = point)
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other === this) {
            return true
        }
        if (other !is ECCurve) {
            return false
        }

        return BigNumContext().use { ctx ->
            val p1 = ctx.get()
            val a1 = ctx.get()
            val b1 = ctx.get()
            val p2 = ctx.get()
            val a2 = ctx.get()
            val b2 = ctx.get()
            EC_GROUP_get_curve(
                native,
                p1.ptr,
                a1.ptr,
                b1.ptr,
                ctx.ptr,
            ).checkTrue("EC_GROUP_get_curve fails")

            EC_GROUP_get_curve(
                other.native,
                p2.ptr,
                a2.ptr,
                b2.ptr,
                ctx.ptr,
            ).checkTrue("EC_GROUP_get_curve fails")
            p1 == p2 && a1 == a2 && b1 == b2
        }
    }

    override fun hashCode(): Int = BigNumContext().use { ctx ->
        val p1 = ctx.get()
        val a1 = ctx.get()
        val b1 = ctx.get()
        p1.calcHashCode() + a1.calcHashCode() * 32 + b1.calcHashCode() * 64
    }

    private fun getFieldNum() = BigNum(EC_GROUP_get0_field(native) ?: throwError("EC_GROUP_get0_field fails"))

    actual val fieldSizeInBits: Int
        get() = getFieldNum().sizeInBits
    actual val fieldSizeInBytes: Int
        get() = getFieldNum().sizeInBytes
}

fun BigInteger.Companion.fromUnsignedByteArray(buf: ByteArray, off: Int, length: Int): BigInteger {
    var mag = buf
    if (off != 0 || length != buf.size) {
        mag = ByteArray(length)
        buf.copyInto(
            destination = mag,
            destinationOffset = 0,
            startIndex = off,
            endIndex = off + length,
        )
    }
    return fromByteArray(mag, Sign.POSITIVE)
}
