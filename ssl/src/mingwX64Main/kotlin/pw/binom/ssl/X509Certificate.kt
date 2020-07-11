package pw.binom.ssl

import cnames.structs.stack_st_X509
import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Closeable

actual class X509Certificate internal constructor(val ptr: CPointer<X509>) : Closeable {
    override fun close() {
        X509_free(ptr)
    }

    actual companion object {
        actual fun load(data: ByteArray): X509Certificate {
            val bio = Bio.mem()
            bio.write(data)
            bio.cursor = 0
            val bbb = PEM_read_bio_X509(bio.self, null, null, null)!!
            return X509Certificate(bbb)
        }
    }

    actual fun save(): ByteArray {
        val bio = Bio.mem()
        PEM_write_bio_X509(bio.self, ptr)
        val stream = ByteArrayOutput()
        bio.copyTo(stream)
        bio.close()
        stream.data.flip()
        val array = stream.data.toByteArray()
        stream.close()
        return array
    }
}

inline val CPointer<stack_st_X509>.size: Int
    get() = sk_X509_num(this).let { if (it < 0) 0 else it }

inline operator fun CPointer<stack_st_X509>.get(index: Int): CPointer<X509> {
    if (index < 0 || index >= size)
        throw IndexOutOfBoundsException()
    return sk_X509_value(this, index)!!
}

inline fun CPointer<stack_st_X509>.forEach(func: (CPointer<X509>) -> Unit) {
    for (i in 0 until size)
        func(this[i])
}