package pw.binom.io

/**
 * Lazy AsyncInputStream. On first call [read] or [close] will init real stream via function [func].
 * Next calls will send to result of function [func]
 *
 * @param func real thread provider
 */
class LazyAsyncInput(private val func: suspend () -> AsyncInput) : AsyncInput {

    var stream: AsyncInput? = null
        private set

    private suspend fun inited(): AsyncInput {
        if (stream == null) {
            stream = func()
        }
        return stream!!
    }

    override val available: Int
        get() = stream?.available ?: -1

    override suspend fun read(dest: ByteBuffer): Int {
        val stream = inited()
        return stream.read(dest)
    }

    override suspend fun asyncClose() {
        inited().asyncClose()
    }
}
