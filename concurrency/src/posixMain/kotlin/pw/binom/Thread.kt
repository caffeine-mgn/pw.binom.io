package pw.binom

import kotlinx.cinterop.*
import platform.posix.pthread_create
import platform.posix.pthread_join
import platform.posix.pthread_t
import platform.posix.pthread_tVar

actual abstract class Thread {
    private var _id: pthread_t = 0.convert()
    actual abstract fun execute()

    actual fun start() {
        memScoped {
            val id = alloc<pthread_tVar>()
            val ptr = StableRef.create(this@Thread)
            if (pthread_create(id.ptr, null, func, ptr.asCPointer()) != 0) {
                ptr.dispose()
                throw IllegalArgumentException("Can't start thread")
            }
            this@Thread._id = id.value
        }
    }

    actual val id: Long
        get() = _id.toLong()

    actual fun join() {
        pthread_join(_id, null)
    }
}

private val func: CPointer<CFunction<(COpaquePointer?) -> COpaquePointer?>> =
    staticCFunction { ptr ->
        val thread = ptr!!.asStableRef<Thread>()
        try {
            thread.get().execute()
        } finally {
            thread.dispose()
        }
        null
    }
