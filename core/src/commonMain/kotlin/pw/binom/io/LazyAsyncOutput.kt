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
class LazyAsyncOutput(private val func: suspend () -> AsyncOutput) : AsyncOutput {

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        val vv = inited()
//        return vv.write(data, offset, length)
//    }

    override suspend fun write(data: ByteBuffer): Int {
        val vv = inited()
        return vv.write(data)
    }

    override suspend fun flush() {
        val vv = inited()
        vv.flush()
    }

    override suspend fun asyncClose() {
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