package pw.binom.charset

import pw.binom.io.Closeable
import pw.binom.pool.*
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private class EncoderFactory(val name: String) : ObjectFactory<IncovCharsetEncoder> {
    override fun allocate(pool: ObjectPool<IncovCharsetEncoder>): IncovCharsetEncoder =
        IncovCharsetEncoder(name) { pool.recycle(it as IncovCharsetEncoder) }

    override fun deallocate(value: IncovCharsetEncoder, pool: ObjectPool<IncovCharsetEncoder>) {
        value.free()
    }
}

private class DecoderFactory(val name: String) : ObjectFactory<IconvCharsetDecoder> {
    override fun allocate(pool: ObjectPool<IconvCharsetDecoder>): IconvCharsetDecoder =
        IconvCharsetDecoder(name) { pool.recycle(it as IconvCharsetDecoder) }

    override fun deallocate(value: IconvCharsetDecoder, pool: ObjectPool<IconvCharsetDecoder>) {
        value.free()
    }
}

@OptIn(ExperimentalTime::class)
class IconvCharset(override val name: String) : Charset, Closeable {

    private val encodePool = GenericObjectPool(factory = EncoderFactory(name), initCapacity = 0)
    private val decodePool = GenericObjectPool(factory = DecoderFactory(name), initCapacity = 0)

    var lastActive = TimeSource.Monotonic.markNow()
        private set

    internal fun markActive() {
        lastActive = TimeSource.Monotonic.markNow()
    }

    override fun newDecoder(): CharsetDecoder {
        return decodePool.borrow { it.reset() }
    }

    override fun newEncoder(): CharsetEncoder {
        return encodePool.borrow { it.reset() }
    }

    fun checkTrim() {
        encodePool.checkTrim()
        decodePool.checkTrim()
    }

    init {
        println("IconvCharset: $name NEW")
    }

    override fun close() {
        println("IconvCharset: $name CLOSE")
        try {
            encodePool.close()
        } finally {
            decodePool.close()
        }
    }
}

private class CoderDefaultPool<T : AbstractIconv>(capacity: Int, new: (DefaultPool<T>) -> T) :
    DefaultPool<T>(capacity = capacity, new = new) {
    override fun free(value: T) {
        value.free()
    }
}
