package pw.binom

import org.khronos.webgl.ArrayBuffer
import org.w3c.files.Blob
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

inline fun Blob.arrayBuffer(): Promise<ArrayBuffer> = asDynamic().arrayBuffer()
suspend fun Blob.toArrayBuffer() = suspendCoroutine { coroutine ->
    arrayBuffer().then({ result ->
        coroutine.resume(result)
    }, { exception ->
        coroutine.resumeWithException(exception)
    })
}
