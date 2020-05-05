package pw.binom.io

/**
 * Lazy AsyncInputStream. On first call [read] or [close] will init real stream via function [func].
 * Next calls will send to result of function [func]
 *
 * @param func real thread provider
 */
class LazyAsyncInputStream(private val func: suspend () -> AsyncInputStream) : AsyncInputStream {
    override suspend fun read(): Byte {
        val stream = inited()
        return stream.read()
    }

    var stream: AsyncInputStream? = null
    private set

    private suspend fun inited(): AsyncInputStream {
        if (stream == null) {
            stream = func()
        }
        return stream!!
    }

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
        val stream = inited()
        return stream.read(data, offset, length)
    }

    override suspend fun close() {
        inited().close()
    }

}