package pw.binom.io.file

import kotlinx.cinterop.*
import platform.posix.read
import platform.windows.*
import pw.binom.io.*

actual class FileWatcher:Closeable {
    actual fun watch(
        file: File,
        create: Boolean,
        modify: Boolean,
        delete: Boolean
    ): Closeable {
        FindFirstChangeNotification(file.path.wcstr,0,FILE_NOTIFY_CHANGE_FILE_NAME.convert())
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}