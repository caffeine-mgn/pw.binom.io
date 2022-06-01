package pw.binom

import kotlinx.cinterop.CPointer
import platform.openssl.*

inline class BigNumContext(val ptr: CPointer<BN_CTX>) {

    companion object {
        fun secureNew() = BigNumContext(BN_CTX_secure_new() ?: TODO("Can't create secure BinNumContext"))
    }

    constructor() : this(BN_CTX_new() ?: TODO("Can't create BigNum Context"))

    fun get() = BigNum(BN_CTX_get(ptr)!!)
    fun start() {
        BN_CTX_start(ptr)
    }

    fun end() {
        BN_CTX_end(ptr)
    }

    fun free() {
        BN_CTX_free(ptr)
    }

    /**
     * Calls [start], then [func] and finally calls [end]. Not calls [free]
     */
    inline fun <T> using(func: (BigNumContext) -> T): T = try {
        start()
        func(this)
    } finally {
        end()
    }

    /**
     * Calls [start], then [func]. After that call [end] and [free]
     */
    inline fun <T> use(func: (BigNumContext) -> T): T = try {
        start()
        func(this)
    } finally {
        end()
        this.free()
    }
}
