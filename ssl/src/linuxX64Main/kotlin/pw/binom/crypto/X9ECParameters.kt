package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import platform.openssl.*
import pw.binom.BigNum
import pw.binom.BigNumContext
import pw.binom.throwError
import kotlin.native.internal.createCleaner

actual class X9ECParameters(val ptr: CPointer<EC_GROUP>, autoClean: Boolean) {
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = if (!autoClean) null else createCleaner(ptr) { ptr ->
        EC_GROUP_free(ptr)
    }

    actual val seed: ByteArray?
        get() {
            val bb = EC_GROUP_get0_seed(ptr)!!
            val len = EC_GROUP_get_seed_len(ptr)
            if (len.convert<Int>() == 0) {
                return null
            }
            return bb.readBytes(len.convert())
        }
    actual val n: BigInteger
        get() = BigNum(EC_GROUP_get0_order(ptr)!!).toBigInt()

    actual val h: BigInteger
        get() = BigNum(EC_GROUP_get0_cofactor(ptr)!!).toBigInt()
    actual val g: EcPoint by lazy {
//        EcPoint(EC_POINT_new(ptr)!!, autoClean = true)
        TODO()
    }

    fun getOrder(ctx: BigNumContext): BigInteger {
        val c = BigNum()
        if (EC_GROUP_get_order(ptr, c.ptr, ctx.ptr) <= 0) {
            TODO("Can't get order from EC_GROUP")
        }
        val ret = c.toBigInt()
        c.free()
        return ret
    }

    actual val curve: ECCurve
        get() = ECCurve(EC_GROUP_dup(ptr) ?: throwError("EC_GROUP_dup fails"))
}
