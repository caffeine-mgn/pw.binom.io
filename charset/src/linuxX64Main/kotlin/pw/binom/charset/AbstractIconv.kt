@file:OptIn(UnsafeNumber::class)

package pw.binom.charset

import kotlinx.cinterop.*
import platform.iconv.iconv_close
import platform.iconv.iconv_open
import platform.posix.*
import pw.binom.io.Buffer
import pw.binom.io.Closeable

/*
@Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")
internal object IconvResourcePool {
    private class Item(val res: AbstractIconv.Resource) {
        val lastAccess = getTimeMillis()
    }

    private val data = HashMap<String, HashSet<Item>>()
    private val dataLock = SpinLock()
    private var count = 0
    internal fun get(fromCharset: String, toCharset: String): AbstractIconv.Resource {
        val key = "$fromCharset..$toCharset"
        val out = dataLock.synchronize {
            val exist = data[key]
            if (exist != null) {
                if (exist.isEmpty()) {
                    data.remove(key)
                    count--
                    return@synchronize AbstractIconv.Resource(fromCharset = fromCharset, toCharset = toCharset)
                }
                if (exist.size == 1) {
                    data.remove(key)
                    count--
                    return@synchronize exist.first().res
                }
                val i = exist.first()
                exist.remove(i)
                count--
                return@synchronize i.res
            }
            return@synchronize AbstractIconv.Resource(fromCharset = fromCharset, toCharset = toCharset)
        }
        clean()
        return out
    }

    internal fun reuse(res: AbstractIconv.Resource) {
        val item = Item(res = res)
        count++
        dataLock.synchronize {
            data.getOrPut(res.key) { HashSet() }.add(item)
        }
        clean()
    }

    private var lastClean = getTimeMillis()
    private fun clean() {
        if (count < 20) {
            return
        }
        val now = getTimeMillis()
        if (now - lastClean <= 60_000) {
            return
        }
        dataLock.synchronize {
            data.forEach {
                val forRemove = it.value.filter { item -> now - item.lastAccess > 60_000 }
                it.value.removeAll(forRemove)
                if (forRemove.isNotEmpty()) {
                    forRemove.forEach {
                        it.res.dispose()
                    }
                    count -= forRemove.size
                }
            }
        }
        lastClean = now
    }
}
*/

private var count = 0

/**
 * Abstract Charset convertor. Uses Iconv native library
 */
@Suppress("OPT_IN_IS_NOT_ENABLED")
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

    internal fun free() {
        resource.dispose()
    }

    override fun close() {
        onClose?.invoke(this)
    }

    protected fun iconv(input: Buffer, output: Buffer): CharsetTransformResult {
        return output.refTo(output.position) { outputPtr ->
            input.refTo(input.position) { inputPtr ->
                memScoped {
                    resource.outputAvail.value = (output.remaining * output.elementSizeInBytes).convert()
                    resource.outputPointer.value = outputPtr.getPointer(this).reinterpret()
                    resource.inputPointer.value = inputPtr.getPointer(this).reinterpret()
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
                        r != 0 && errno == E2BIG -> CharsetTransformResult.OUTPUT_OVER
                        r != 0 && errno == EILSEQ -> CharsetTransformResult.MALFORMED
                        r != 0 && errno == EINVAL -> CharsetTransformResult.INPUT_OVER
                        r == 0 && errno == 0 -> CharsetTransformResult.SUCCESS
                        else -> throw IllegalStateException("Iconv Exception. Errno: $errno, Result: $r")
                    }
                }
            }
        } ?: CharsetTransformResult.SUCCESS
    }
}
