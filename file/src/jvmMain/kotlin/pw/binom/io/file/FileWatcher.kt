package pw.binom.io.file

import pw.binom.io.Closeable
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey

actual class FileWatcher : Closeable {
    private val native = FileSystems.getDefault().newWatchService()
    override fun close() {
        native.close()
    }

    actual fun watch(
        file: File,
        create: Boolean,
        modify: Boolean,
        delete: Boolean
    ): Closeable {
        val o = ArrayList<WatchEvent.Kind<*>>()
        if (create) {
            o += StandardWatchEventKinds.ENTRY_CREATE
        }
        if (modify) {
            o += StandardWatchEventKinds.ENTRY_MODIFY
        }
        if (delete) {
            o += StandardWatchEventKinds.ENTRY_DELETE
        }
        val vv = file.java.toPath().register(native, *o.toTypedArray())
        val watcher = Watcher(vv)
        return watcher
    }

    private class Watcher(val key: WatchKey) : Closeable {
        override fun close() {
            key.cancel()
        }
    }

    private class ChangeImpl : Change {
        override var type: ChangeType = ChangeType.MODIFY
        override var file: File = File("")
    }

    private val change = ChangeImpl()

    actual fun pullChanges(func: (Change) -> Unit): Int {
        var count = 0
        while (true) {
            val key = native.take()
            key.pollEvents().forEach {
                change.type = when (it.kind()) {
                    StandardWatchEventKinds.ENTRY_CREATE -> ChangeType.CREATE
                    StandardWatchEventKinds.ENTRY_MODIFY -> ChangeType.MODIFY
                    StandardWatchEventKinds.ENTRY_DELETE -> ChangeType.DELETE
                    else -> TODO()
                }
                change.file = (it.context() as java.io.File).binom
                count++
            }
            if (!key.reset()) {
                break
            }
        }
        return count
    }
}
