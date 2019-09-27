package pw.binom.io

/**
 * Lazy AsyncInputStream. On first call [read] or [close] will init real stream via function [func].
 * Next calls will send to result of function [func]
 *
 * @param func real thread provider
 */
class LazyAsyncInputStream(private val func: suspend () -> AsyncInputStream) : AsyncInputStream {
    override suspend fun read(): Byte =
            inited().read()

    private var inited = false
    private var stream: AsyncInputStream? = null

    private suspend fun inited(): AsyncInputStream {
        if (!inited) {
            stream = func()
            inited = true
        }
        return stream!!
    }

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
            inited().read(data, offset, length)

    override suspend fun close() {
        inited().close()
    }

}