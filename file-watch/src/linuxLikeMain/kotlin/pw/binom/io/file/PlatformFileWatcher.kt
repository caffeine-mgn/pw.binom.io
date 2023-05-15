package pw.binom.io.file

internal actual fun createDefaultFileWatcher(): FileWatcher = InotifyFileWatcher()
