package pw.binom.io.file

import com.sun.nio.file.ExtendedWatchEventModifier
import pw.binom.io.Closeable
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent.Kind as JvmEventKind
import java.nio.file.WatchEvent.Modifier as JvmModifier

class JvmFileWatcher : FileWatcher {
  private val watchService = FileSystems.getDefault().newWatchService()
  override fun register(filePath: File, recursive: Boolean, modes: WatchEventKind): Closeable {
    val path = FileSystems.getDefault().getPath(filePath.path)
    val jvmEvents = arrayOfNulls<JvmEventKind<*>>(modes.size)
    modes.forEachIndexed { index, event ->
      jvmEvents[index] = when (event) {
        WatchEventKind.MODIFY -> StandardWatchEventKinds.ENTRY_MODIFY
        WatchEventKind.CREATE -> StandardWatchEventKinds.ENTRY_CREATE
        WatchEventKind.DELETE -> StandardWatchEventKinds.ENTRY_DELETE
        else -> throw IllegalStateException()
      }
    }
    val m = if (recursive) {
      arrayOf<JvmModifier>(ExtendedWatchEventModifier.FILE_TREE)
    } else {
      emptyArray<java.nio.file.WatchEvent.Modifier>()
    }
    val watchKey = path.register(watchService, jvmEvents, *m)
    return Closeable {
      watchKey.cancel()
    }
  }

  private class WatchEventImpl : WatchEvent {
    override var file = File("")
    override var type: WatchEventKind = WatchEventKind.EMPTY
  }

  private val eventImpl = WatchEventImpl()

  override fun pollEvents(func: (WatchEvent) -> Unit) {
    val wk = watchService.take()
    wk.pollEvents().forEach { event ->
      // we only register "ENTRY_MODIFY" so the context is always a Path.
      val changed = event.context() as Path
      var kind = WatchEventKind.EMPTY
      when (event.kind()) {
        StandardWatchEventKinds.ENTRY_MODIFY -> kind += WatchEventKind.MODIFY
        StandardWatchEventKinds.ENTRY_CREATE -> kind += WatchEventKind.CREATE
        StandardWatchEventKinds.ENTRY_DELETE -> kind += WatchEventKind.DELETE
      }
      eventImpl.file = changed.toFile().binom
      eventImpl.type = kind
    }
    // reset the key
    val valid = wk.reset()
  }

  override fun close() {
    watchService.close()
  }
}
