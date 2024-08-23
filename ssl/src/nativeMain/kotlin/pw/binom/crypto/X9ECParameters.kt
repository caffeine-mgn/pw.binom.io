package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import platform.openssl.*
import pw.binom.BigNum
import pw.binom.BigNumContext
import pw.binom.throwError

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class X9ECParameters(actual val curve: ECCurve) {

  actual val seed: ByteArray?
    get() {
      val bb = EC_GROUP_get0_seed(curve.native)!!
      val len = EC_GROUP_get_seed_len(curve.native)
      if (len.convert<Int>() == 0) {
        return null
      }
      return bb.readBytes(len.convert())
    }
  actual val n: BigInteger
    get() = BigNum(EC_GROUP_get0_order(curve.native)!!).toBigInt()

  actual val h: BigInteger
    get() = BigNum(EC_GROUP_get0_cofactor(curve.native)!!).toBigInt()
  actual val g: EcPoint by lazy {
    val ptr = EC_GROUP_get0_generator(curve.native) ?: throwError("EC_GROUP_get0_generator fails")
    val newPtr = EC_POINT_dup(ptr, curve.native) ?: throwError("EC_POINT_dup fails")
    EcPoint(curve, newPtr)
  }

  fun getOrder(ctx: BigNumContext): BigInteger {
    val c = BigNum()
    if (EC_GROUP_get_order(curve.native, c.ptr, ctx.ptr) <= 0) {
      TODO("Can't get order from EC_GROUP")
    }
    val ret = c.toBigInt()
    c.free()
    return ret
  }
}
