package pw.binom.io.file

/*
actual class FileWatcher : Closeable {
    private var closed = false
    private val native = FileSystems.getDefault().newWatchService()
    override fun close() {
        checkClosed()
        closed = true
        native.close()
    }

    private fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

    actual fun watch(
        file: File,
        create: Boolean,
        modify: Boolean,
        delete: Boolean
    ): Closeable {
        checkClosed()
        val o = ArrayList<WatchEvent.Kind<*>>(3)
        if (create) {
            o += StandardWatchEventKinds.ENTRY_CREATE
        }
        if (modify) {
            o += StandardWatchEventKinds.ENTRY_MODIFY
        }
        if (delete) {
            o += StandardWatchEventKinds.ENTRY_DELETE
        }
        val key = file.java.toPath().register(native, *o.toTypedArray())
        val watcher = Watcher(rootFile = file, key = key)
        nativeToBinom[key] = watcher
        return watcher
    }

    private val nativeToBinom = HashMap<WatchKey, Watcher>()

    private inner class Watcher(val rootFile: File, val key: WatchKey) : Closeable {
        override fun close() {
            nativeToBinom.remove(key)
            if (key.isValid) {
                key.cancel()
            }
        }
    }

    private class ChangeImpl : Change {
        override var type: ChangeType = ChangeType.MODIFY
        override var file: File = File("")
    }

    private val change = ChangeImpl()

    actual fun pullChanges(func: (Change) -> Unit): Int {
        return eventProcessing(key = native.take(), func = func)
    }

    private fun eventProcessing(key: WatchKey, func: (Change) -> Unit): Int {
        var count = 0
        key.pollEvents().forEach {
            change.type = when (it.kind()) {
                StandardWatchEventKinds.ENTRY_CREATE -> ChangeType.CREATE
                StandardWatchEventKinds.ENTRY_MODIFY -> ChangeType.MODIFY
                StandardWatchEventKinds.ENTRY_DELETE -> ChangeType.DELETE
                else -> TODO()
            }
            val path = it.context() as Path
            val binomWatcher = nativeToBinom[key] ?: throw IllegalStateException("Can't find binom FileWatcher")
            change.file = binomWatcher.rootFile.relative(path.name)
            func(change)
            count++
        }
        if (!key.reset()) {
            nativeToBinom[key]?.close()
        }
        return count
    }

    actual fun pullChanges(timeout: Duration, func: (Change) -> Unit): Int {
        var count = 0
        while (true) {
            val key = native.poll(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS) ?: break
            count += eventProcessing(key = key, func = func)
        }
        return count
    }
}
*/
