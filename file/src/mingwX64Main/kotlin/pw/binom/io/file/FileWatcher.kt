package pw.binom.io.file

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.windows.*
import pw.binom.io.*
import kotlin.math.roundToInt

class PointerList(val capacityFactor: Float = 1.7f) : Closeable {
    var data = nativeHeap.allocArray<COpaquePointerVar>(0)
        private set
    var capacity = 0
        private set
    var size = 0
        private set

    fun add(pointer: COpaquePointer) {
        if (size < capacity) {
            data[size++] = pointer
        } else {
            val newSize = (maxOf(capacity, 3) * capacityFactor).roundToInt()
            val newData = nativeHeap.allocArray<COpaquePointerVar>(newSize)
            memcpy(newData, data, (capacity * sizeOf<COpaquePointerVar>()).convert())
            capacity = newSize
            nativeHeap.free(data)
            data = newData
        }
    }

    fun delete(index: Int) {
        if (index > size) {
            TODO()
        }
        if (index < 0) {
            TODO()
        }
        if (index == size - 1) {
            data[--size] = NULL
            return
        }
        if (index == 0) {
            val newData = nativeHeap.allocArray<COpaquePointerVar>(capacity)
            memcpy(
                newData,
                data.pointed.ptr + sizeOf<COpaquePointerVar>(),
                ((size - 1) * sizeOf<COpaquePointerVar>()).convert()
            )
            nativeHeap.free(data)
            data = newData
        }
    }

    operator fun get(index: Int) = data[index]
    operator fun set(index: Int, value: COpaquePointer) {
        data[index] = value
    }

    override fun close() {
        capacity = 0
        size = 0
        nativeHeap.free(data)
    }
}

actual class FileWatcher : Closeable {

    private val watchers = HashMap<COpaquePointer, Watcher>()
    private val headers = PointerList()

    private inner class Watcher(val ptr: COpaquePointer, val path: File) : Closeable {
        override fun close() {
            FindCloseChangeNotification(ptr)
        }
    }

    actual fun watch(
        file: File,
        create: Boolean,
        modify: Boolean,
        delete: Boolean
    ): Closeable {
        val handler = memScoped {
            FindFirstChangeNotification!!(file.path.wcstr.getPointer(this), 0, FILE_NOTIFY_CHANGE_FILE_NAME.convert())
        }
        if (handler == INVALID_HANDLE_VALUE || handler == null) {
            throw IOException("Couldn't add watch to ${file.path}")
        }
        headers.add(handler)
        val watcher = Watcher(
            ptr = handler,
            path = file
        )
        watchers[handler] = watcher
        return watcher
    }

    private class ChangeImpl : Change {
        override var type: ChangeType = ChangeType.CREATE
        override var file: File = File("")
    }

    private val change = ChangeImpl()

    actual fun pullChanges(func: (Change) -> Unit): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        watchers.values.forEach {
            it.close()
        }
        watchers.clear()
        headers.close()
    }
}
