package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput
import pw.binom.ByteBuffer

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

//    override suspend fun skip(length: Long): Long {
//        val stream = inited()
//        return stream.skip(length)
//    }

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        val stream = inited()
//        return stream.read(data, offset, length)
//    }

    override suspend fun read(dest: ByteBuffer): Int {
        val stream = inited()
        return stream.read(dest)
    }

    override suspend fun close() {
        inited().close()
    }

}