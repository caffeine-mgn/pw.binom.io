package pw.binom.io.file

import pw.binom.io.Closeable

interface FileWatcher : Closeable {
    companion object {
        fun createDefault() = createDefaultFileWatcher()
    }

    fun register(filePath: File, recursive: Boolean, modes: WatchEventKind): Closeable
    fun pollEvents(func: (WatchEvent) -> Unit)
}
