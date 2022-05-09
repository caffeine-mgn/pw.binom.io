@file:JvmName("FileWatcherCommonKt")

package pw.binom.io.file

import kotlin.jvm.JvmName

/*
expect class FileWatcher : Closeable {
    constructor()
    fun watch(file: File, create: Boolean, modify: Boolean, delete: Boolean): Closeable
    fun pullChanges(func: (Change) -> Unit): Int
    fun pullChanges(timeout: Duration, func: (Change) -> Unit): Int
}

interface Change {
    val type: ChangeType
    val file: File
}

enum class ChangeType {
    CREATE, MODIFY, DELETE
}
*/
