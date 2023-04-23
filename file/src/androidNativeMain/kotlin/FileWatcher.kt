package pw.binom.io.file

/*
private const val MAX_EVENTS = 1024 */
/*Максимальное кличество событий для обработки за один раз*//*


actual class FileWatcher : Closeable {

    private val inotifyId = inotify_init()

    private inner class Watcher(val id: Int, val root: File) : Closeable {
        var closed = false
        override fun close() {
            if (closed) {
                throw ClosedException()
            }
            inotify_rm_watch(inotifyId, id)
            closed = true
        }
    }

    init {
        if (inotifyId < 0) {
            throw IOException("Couldn't initialize inotify")
        }
    }

    private val buffer = nativeHeap.allocArray<inotify_event>(MAX_EVENTS)

    private class ChangeImpl : Change {
        override var type: ChangeType = ChangeType.CREATE
        override var file: File = File("")
    }

    private val change = ChangeImpl()
    private val watchers = HashMap<Int, Watcher>()

    actual fun watch(file: File, create: Boolean, modify: Boolean, delete: Boolean): Closeable {
        var result = 0
        if (create) {
            result = result or IN_CREATE
        }
        if (modify) {
            result = result or IN_MODIFY
        }
        if (modify) {
            result = result or IN_DELETE
        }
        val watchId = inotify_add_watch(inotifyId, file.path, result.convert())
        if (watchId == -1) {
            throw IOException("Couldn't add watch to ${file.path}")
        }
        val watcher = Watcher(id = watchId, root = file)
        watchers[watchId] = watcher
        return watcher
    }

    override fun close() {
        watchers.values.forEach {
            if (!it.closed) {
                it.close()
            }
        }
        watchers.clear()
        nativeHeap.free(buffer)
        platform.posix.close(inotifyId)
    }

    actual fun pullChanges(func: (Change) -> Unit): Int {
        val length = read(inotifyId, buffer, (MAX_EVENTS * sizeOf<inotify_event>()).convert())
        if (length <= 0) {
            return 0
        }
        val count = length / sizeOf<inotify_event>()

        for (i in 0 until count) {
            val event = buffer[i]
            val watcher = watchers[event.wd]!!
            change.file = watcher.root.relative(event.name.toKString())
            val type = when {
                event.mask.toInt() and IN_CREATE != 0 -> ChangeType.CREATE
                event.mask.toInt() and IN_MODIFY != 0 -> ChangeType.MODIFY
                event.mask.toInt() and IN_DELETE != 0 -> ChangeType.DELETE
                else -> throw IOException("Unknown mask event ${change.file}")
            }
            func(change)
        }
        return count.convert()
    }
}
*/
