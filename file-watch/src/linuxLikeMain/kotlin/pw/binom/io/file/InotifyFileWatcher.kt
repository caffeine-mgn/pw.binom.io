package pw.binom.io.file

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.read
import pw.binom.io.Closeable
import pw.binom.io.IOException

private const val ELEMENTS_COUNT = 100
private val EVENT_SIZE = (sizeOf<inotify_event>())
private val BUF_LEN = (ELEMENTS_COUNT * (EVENT_SIZE + 16))

class InotifyFileWatcher : FileWatcher {

    val notifyFd = inotify_init1(IN_CLOEXEC)

    init {
        if (notifyFd < 0) {
            throw IOException("Can't create inotify")
        }
    }

    private val buffer = nativeHeap.allocArray<ByteVar>(BUF_LEN)

    private inner class WatchLi(
        val file: File,
        val fd: Int,
        val isFile: Boolean,
        val recursive: Boolean,
        val modes: WatchEventKind,
        val name: String,
    ) : Closeable {
        var parent: WatchLi? = null
        val childs = HashMap<String, WatchLi>()
        override fun close() {
            parent?.childs?.remove(name)
            watchers -= fd
            inotify_rm_watch(notifyFd, fd.convert())
        }
    }

    private val watchers = HashMap<Int, WatchLi>()

    private fun internalRegister(filePath: File, recursive: Boolean, modes: WatchEventKind): WatchLi {
        var rr = 0
        if (modes.isCreate) {
            rr = rr or IN_CREATE
        }
        if (modes.isModify) {
            rr = rr or IN_MODIFY
        }
        if (modes.isDelete) {
            rr = rr or IN_DELETE
        }
        val wd = inotify_add_watch(
            notifyFd,
            filePath.path,
            rr.convert(),
        )
        if (wd < 0) {
            throw IOException("Can't register watching \"$filePath\"")
        }
        val listener = WatchLi(
            file = filePath,
            isFile = filePath.isFile,
            recursive = recursive,
            modes = modes,
            fd = wd,
            name = filePath.name,
        )

        watchers[wd] = listener
        if (recursive && filePath.isDirectory) {
            filePath.list().forEach { file ->
                if (file.isDirectory) {
                    val l = internalRegister(
                        filePath = file,
                        recursive = true,
                        modes = modes,
                    )
                    l.parent = listener
                    listener.childs[file.name] = l
                }
            }
        }
        return listener
    }

    private class WatchEventImpl : WatchEvent {
        override var file: File = File("")
        override var type: WatchEventKind = WatchEventKind.EMPTY
    }

    private val eventImpl = WatchEventImpl()
    override fun register(filePath: File, recursive: Boolean, modes: WatchEventKind): Closeable {
        if (modes.isEmpty) {
            return Closeable.STUB
        }
        return internalRegister(
            filePath = filePath,
            recursive = recursive,
            modes = modes,
        )
    }

    private fun convertEventType(modeMask: UInt): WatchEventKind {
        var eventKind = WatchEventKind.EMPTY
        if (modeMask and IN_CREATE.convert() != 0u) {
            eventKind += WatchEventKind.CREATE
        }
        if (modeMask and IN_MODIFY.convert() != 0u) {
            eventKind += WatchEventKind.MODIFY
        }
        if (modeMask and IN_DELETE.convert() != 0u || modeMask and IN_DELETE_SELF.convert() != 0u || modeMask and IN_IGNORED.convert() != 0u) {
            eventKind += WatchEventKind.DELETE
        }
        return eventKind
    }

    override fun pollEvents(func: (WatchEvent) -> Unit) {
        val length = read(notifyFd, buffer, BUF_LEN.convert())
        var cursor = 0L
        while (cursor < length) {
            val event = (buffer + cursor)!!.reinterpret<inotify_event>()
            val e = event.pointed
            if (e.len.toInt() > 0) {
                val root = watchers[e.wd] ?: continue
                val eventKind = convertEventType(e.mask)
                if (eventKind.isDelete) {
                    val fileName = e.name.toKString()
                    root.childs.remove(fileName)?.close()
                }

                eventImpl.file = if (root.isFile) {
                    root.file
                } else {
                    val fileName = e.name.toKString()
                    val fullPath = root.file.relative(fileName)
                    if (root.recursive && eventKind.isCreate && !root.isFile) {
                        val listener = internalRegister(filePath = fullPath, recursive = true, modes = root.modes)
                        listener.parent = root
                        root.childs[fileName] = listener
                    }
                    fullPath
                }
                eventImpl.type = eventKind
                func(eventImpl)
            } else {
                val root = watchers[e.wd] ?: continue
                if (root.isFile) {
                    root.close()
                }
                eventImpl.file = root.file
                eventImpl.type = convertEventType(e.mask)
                func(eventImpl)
            }
            cursor += EVENT_SIZE
        }
    }

    override fun close() {
        platform.posix.close(notifyFd)
        nativeHeap.free(buffer)
    }
}
