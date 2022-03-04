package pw.binom.io.file

import pw.binom.io.Closeable

expect class FileWatcher : Closeable {
    fun watch(file: File, create: Boolean, modify: Boolean, delete: Boolean): Closeable
    fun pullChanges(func: (Change) -> Unit):Int




}

interface Change {
    val type: ChangeType
    val file: File
}

enum class ChangeType {
    CREATE, MODIFY, DELETE
}