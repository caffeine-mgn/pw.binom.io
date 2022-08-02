package pw.binom.charset

import pw.binom.pool.DefaultPool

class IconvCharset(override val name: String) : Charset {
    private val encodePool =
        CoderDefaultPool<IncovCharsetEncoder>(64) { pool -> IncovCharsetEncoder(name) { self -> pool.recycle(self as IncovCharsetEncoder) } }
    private val decodePool =
        CoderDefaultPool<IconvCharsetDecoder>(64) { pool -> IconvCharsetDecoder(name) { self -> pool.recycle(self as IconvCharsetDecoder) } }

    override fun newDecoder(): CharsetDecoder {
        return decodePool.borrow()
    }

    override fun newEncoder(): CharsetEncoder {
        return encodePool.borrow()
    }
}

private class CoderDefaultPool<T : AbstractIconv>(capacity: Int, new: (DefaultPool<T>) -> T) :
    DefaultPool<T>(capacity = capacity, new = new) {
    override fun free(value: T) {
        value.free()
    }
}
