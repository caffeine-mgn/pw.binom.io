package pw.binom

import kotlinx.cinterop.*
import platform.windows.*

actual abstract class Thread {
    private var _id: HANDLE? = null
    actual abstract fun execute()

    actual fun start() {
        val ptr = StableRef.create(this@Thread)
        val id2 = CreateThread(null, 0, func, ptr.asCPointer(), 0, null)
        if (id2 == null) {
            ptr.dispose()
            throw IllegalArgumentException("Can't start thread")
        }
        this@Thread._id = id2
    }

    actual val id: Long
        get() = _id.toLong()

    actual fun join() {
        WaitForSingleObject(_id, INFINITE)
    }
}

private val func: CPointer<CFunction<(COpaquePointer?) -> DWORD>> =
    staticCFunction { ptr ->
        val thread = ptr!!.asStableRef<Thread>()
        try {
            thread.get().execute()
        } finally {
            thread.dispose()
        }
        0.convert()
    }
