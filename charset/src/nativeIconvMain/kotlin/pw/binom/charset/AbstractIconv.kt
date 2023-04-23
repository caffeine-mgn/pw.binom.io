package pw.binom.charset

import kotlinx.cinterop.*
import platform.iconv.iconv_close
import platform.iconv.iconv_open
import platform.posix.*
import pw.binom.io.Buffer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException

/**
 * Abstract Charset convertor. Uses Iconv native library
 */
abstract class AbstractIconv(
    fromCharset: String,
    toCharset: String,
    val onClose: ((AbstractIconv) -> Unit)?
) : Closeable {

    internal class Resource(fromCharset: String, toCharset: String) {

        //        val key = "$fromCharset..$toCharset"
        val iconvHandle = iconv_open(toCharset, fromCharset)
        val inputAvail = nativeHeap.alloc<size_tVar>()
        val outputAvail = nativeHeap.alloc<size_tVar>()
        val outputPointer = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()
        val inputPointer = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()

        init {
            set_posix_errno(0)
            val r = platform.iconv.iconv(
                iconvHandle,
                null,
                null,
                outputPointer.ptr.reinterpret(),
                outputAvail.ptr
            ).toInt()
            if (r == -1 && errno == EBADF) {
                throw IllegalArgumentException("Charset not supported")
            }
        }

        fun dispose() {
            iconv_close(iconvHandle)
            nativeHeap.free(inputAvail)
            nativeHeap.free(outputAvail)
            nativeHeap.free(outputPointer)
            nativeHeap.free(inputPointer)
        }
    }

    private val resource = Resource(fromCharset, toCharset)

    internal open fun reset() {
        closed = false
    }

    internal open fun free() {
        resource.dispose()
    }

    private var closed = false

    override fun close() {
        if (closed) {
            throw ClosedException()
        }
        try {
            onClose?.invoke(this)
        } finally {
            closed = true
        }
    }

    protected fun iconv(input: Buffer, output: Buffer): CharsetTransformResult {
        return output.refTo(output.position) { outputPtr ->
            input.refTo(input.position) inputRefTo@{ inputPtr ->
                var callCount = 0
                while (true) {
                    callCount++
                    if (callCount > 100) {
                        throw IllegalStateException("Iconv loop happened")
                    }
                    resource.outputAvail.value = (output.remaining * output.elementSizeInBytes).convert()
                    resource.outputPointer.value = outputPtr.reinterpret<CPointerVar<ByteVar>>()
                    resource.inputPointer.value = inputPtr.reinterpret<CPointerVar<ByteVar>>()
                    resource.inputAvail.value = (input.remaining * input.elementSizeInBytes).convert()
                    set_posix_errno(0)

                    val beforeIn = resource.inputAvail.value.toInt()
                    val beforeOut = resource.outputAvail.value.toInt()
                    val r = platform.iconv.iconv(
                        resource.iconvHandle,

                        resource.inputPointer.ptr.reinterpret(),
                        resource.inputAvail.ptr,

                        resource.outputPointer.ptr.reinterpret(),
                        resource.outputAvail.ptr
                    ).toInt()
                    val readed = beforeIn - resource.inputAvail.value.toInt()
                    val writed = beforeOut - resource.outputAvail.value.toInt()
                    input.position += readed / input.elementSizeInBytes
                    output.position += writed / output.elementSizeInBytes
                    when {
                        r != 0 && errno == E2BIG -> return@inputRefTo CharsetTransformResult.OUTPUT_OVER
                        r != 0 && errno == EILSEQ -> return@inputRefTo CharsetTransformResult.MALFORMED
                        r != 0 && errno == EINVAL -> return@inputRefTo CharsetTransformResult.INPUT_OVER
                        r == 0 && errno == EAGAIN -> continue
                        r == 0 && errno == 0 -> return@inputRefTo CharsetTransformResult.SUCCESS
                        else -> error("Iconv Exception. Errno: $errno, Result: $r")
                    }
                }
                throw IllegalStateException()
            }
        } ?: CharsetTransformResult.SUCCESS
    }
}
