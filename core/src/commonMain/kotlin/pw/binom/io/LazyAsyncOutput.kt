package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer

/**
 * Lazy AsyncOutputStream. On first call [write], [flush] or [close] will init real stream via function [func].
 * Next calls will send to result of function [func]
 *
 * @param func real thread provider
 */
@Deprecated(message = "Not use it")
class LazyAsyncOutput(private val func: suspend () -> AsyncOutput) : AsyncOutput {

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        val vv = inited()
//        return vv.write(data, offset, length)
//    }

    override suspend fun write(data: ByteBuffer): Int {
        checkClosed()
        val vv = inited()
        return vv.write(data)
    }

    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override suspend fun flush() {
        checkClosed()
        val vv = inited()
        vv.flush()
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
        val vv = inited()
        vv.asyncClose()
    }

    private var inited = false
    private var stream: AsyncOutput? = null

    private suspend fun inited(): AsyncOutput {
        if (!inited) {
            stream = func()
            inited = true
        }
        return stream!!
    }
}