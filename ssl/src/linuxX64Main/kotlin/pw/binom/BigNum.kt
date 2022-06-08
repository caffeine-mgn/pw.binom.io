package pw.binom

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import kotlinx.cinterop.*
import platform.openssl.*

value class BigNum(val ptr: CPointer<BIGNUM>) {
    constructor() : this(BN_new() ?: throwError("Can't create BigNum"))
    constructor(ctx: BigNumContext) : this(BN_CTX_get(ctx.ptr)!!)

    fun setPositive() {
        BN_set_negative(ptr, 0)
    }

    fun copy(dest: BigNum) {
        BN_copy(dest.ptr, ptr) ?: throwError("Can't copy BigNum")
    }

    fun add(other: BigNum, dest: BigNum) {
        BN_add(r = dest.ptr, a = ptr, b = other.ptr).checkTrue("Can't add BigNum to BigNum")
    }

    fun add(other: BigNum): BigNum {
        val ret = BigNum()
        add(other = other, dest = ret)
        return ret
    }

    fun sub(other: BigNum, dest: BigNum) {
        BN_sub(r = dest.ptr, a = ptr, b = other.ptr).checkTrue("Can't sub BigNum to BigNum")
    }

    operator fun minusAssign(other: BigNum) {
        sub(other, this)
    }

    operator fun timesAssign(value: ULong) {
        BN_mul_word(ptr, value).checkTrue("BN_mul_word fails")
    }

    operator fun plusAssign(other: BigNum) {
        add(other = other, dest = this)
    }

    fun sub(other: BigNum): BigNum {
        val ret = BigNum()
        sub(other = other, dest = ret)
        return ret
    }

    fun mul(other: BigNum, dest: BigNum, ctx: BigNumContext? = null) {
        BN_mul(ptr, other.ptr, dest.ptr, ctx?.ptr).checkTrue("Can't multiple BigNum to BigNum")
    }

    fun mul(other: BigNum, ctx: BigNumContext? = null): BigNum {
        val ret = BigNum()
        mul(
            other = other,
            dest = ret,
            ctx = ctx,
        )
        return ret
    }

    fun copy() = BigNum(BN_dup(ptr) ?: TODO("Can't create BigNum duplicating other BigNum"))
    val sizeInBytes
        get() = internal_BN_num_bytes(ptr).convert<Int>()

    val sizeInBits
        get() = BN_num_bits(ptr).convert<Int>()

    fun setNegative() {
        BN_set_negative(ptr, 0)
    }

    /**
     * Using [BN_bin2bn] sets [data] to this BigNum
     */
    fun setByteArray(data: ByteArray) {
        data.usePinned { p ->
            BN_bin2bn(p.addressOf(0).reinterpret(), p.get().size.convert(), ptr)
                ?: throwError("Can't set byte[] to BugNum")
        }
    }

    fun shr(dest: BigNum, n: Int) {
        BN_rshift(dest.ptr, ptr, n).checkTrue("BN_rshift fails")
    }

    fun shl(dest: BigNum, n: Int) {
        BN_lshift(dest.ptr, ptr, n).checkTrue("BN_lshift fails")
    }

    fun shr(n: Int) {
        shr(this, n)
    }

    fun shl(n: Int) {
        shl(this, n)
    }

    operator fun compareTo(other: BigNum): Int {
        return BN_cmp(ptr, other.ptr)
    }

    fun getByteArray(): ByteArray {
        val mem = ByteArray(internal_BN_num_bytes(ptr).convert())
        mem.usePinned { p ->
            BN_bn2bin(ptr, p.addressOf(0).reinterpret()).checkTrue("Can't extract byte[] from BigNum")
        }
        return mem
    }

    fun setZero() {
        internal_BN_zero(ptr)
    }

    val isPositive
        get() = if (isZero) {
            false
        } else {
            BN_is_negative(ptr) <= 0
        }

    val isNegative
        get() = if (isZero) {
            false
        } else {
            BN_is_negative(ptr) > 0
        }

    val isZero
        get() = BN_is_zero(ptr) > 0

    fun free() {
        BN_free(ptr)
    }

    fun toBigInt(): BigInteger {
        val signum = when {
            isZero -> Sign.ZERO
            isNegative -> Sign.NEGATIVE
            else -> Sign.POSITIVE
        }
        return BigInteger.fromByteArray(getByteArray(), signum)
    }

    fun calcHashCode() = getByteArray().fold(0) { acc, value -> acc + value }

    override fun toString() = BN_bn2dec(ptr)!!.toKStringFromUtf8()

    inline fun <T> use(func: (BigNum) -> T): T = try {
        func(this)
    } finally {
        this.free()
    }
}

fun CPointer<BIGNUM>.toBigInteger() = BigNum(this).toBigInt()

fun BigInteger.toBigNum(ptr: BigNum) {
    when (getSign()) {
        Sign.ZERO -> ptr.setZero()
        Sign.POSITIVE -> ptr.setPositive()
        Sign.NEGATIVE -> ptr.setNegative()
    }
    ptr.setByteArray(toByteArray())
}

fun BigInteger.toBigNum(ctx: BigNumContext): BigNum {
    val ret = ctx.get()
    toBigNum(ret)
    return ret
}

fun BigInteger.toBigNum(): BigNum {
    val ret = BigNum()
    toBigNum(ret)
    return ret
}
