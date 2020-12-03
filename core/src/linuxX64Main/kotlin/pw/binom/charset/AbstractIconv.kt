package pw.binom.charset

import kotlinx.cinterop.*
import platform.iconv.iconv_close
import platform.iconv.iconv_open
import platform.posix.*
import pw.binom.Buffer
import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.io.Closeable

const val NATIVE_CHARSET = "wchar_t"

/**
 * Abstract Charset convertor. Uses Iconv native library
 */
abstract class AbstractIconv(fromCharset: String, toCharset: String) : Closeable {
    private val iconvHandle = iconv_open(toCharset, fromCharset)
    private val inputAvail = nativeHeap.alloc<size_tVar>()
    private val outputAvail = nativeHeap.alloc<size_tVar>()
    private val outputPointer = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()
    private val inputPointer = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()

    init {
        platform.iconv.iconv(
                iconvHandle,
                null,
                null,
                outputPointer.ptr.reinterpret(),
                outputAvail.ptr
        )
    }

    protected fun iconv(input: Buffer, output: Buffer): CharsetTransformResult {
        memScoped {
            outputAvail.value = output.remaining.convert()
            outputPointer.value = output.refTo(output.position).getPointer(this).reinterpret()
            inputPointer.value = input.refTo(input.position).getPointer(this).reinterpret()
            inputAvail.value = input.remaining.convert()
            set_posix_errno(0)

            val beforeIn = inputAvail.value.toInt()
            val beforeOut = outputAvail.value.toInt()
            val r = platform.iconv.iconv(
                    iconvHandle,

                    inputPointer.ptr.reinterpret(),
                    inputAvail.ptr,

                    outputPointer.ptr.reinterpret(),
                    outputAvail.ptr
            ).toInt()
            val readed = beforeIn - inputAvail.value.toInt()
            val writed = beforeOut - outputAvail.value.toInt()
            input.position += readed
            output.position += writed
            return when {
                r != 0 && errno == E2BIG -> CharsetTransformResult.OUTPUT_OVER
                r != 0 && errno == EILSEQ -> CharsetTransformResult.MALFORMED
                r != 0 && errno == EINVAL -> CharsetTransformResult.INPUT_OVER
                r == 0 && errno == 0 -> CharsetTransformResult.SUCCESS
                else -> throw IllegalStateException()
            }
        }
    }

    override fun close() {
        iconv_close(iconvHandle)
        nativeHeap.free(inputAvail)
        nativeHeap.free(outputAvail)
        nativeHeap.free(outputPointer)
        nativeHeap.free(inputPointer)
    }
}