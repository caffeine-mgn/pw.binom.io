package pw.binom.io

/**
 * Lazy AsyncOutputStream. On first call [write], [flush] or [close] will init real stream via function [func].
 * Next calls will send to result of function [func]
 *
 * @param func real thread provider
 */
class LazyAsyncOutputStream(private val func: suspend () -> AsyncOutputStream) : AsyncOutputStream {
    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
        val vv = inited()
        return vv.write(data, offset, length)
    }

    override suspend fun flush() {
        val vv = inited()
        vv.flush()
    }

    override suspend fun close() {
        val vv = inited()
        vv.close()
    }

    private var inited = false
    private var stream: AsyncOutputStream? = null

    private suspend fun inited(): AsyncOutputStream {
        if (!inited) {
            stream = func()
            inited = true
        }
        return stream!!
    }
}