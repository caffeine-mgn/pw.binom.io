package pw.binom.io.file

import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test

class FileWatcherTest {
  @Test
  fun aa() {
    try {
      val watcher = FileWatcher.createDefault()
      val dir = File.temporalDirectory!!.relative(Random.nextUuid().toShortString())
      dir.mkdirs()
      val e = watcher.register(
        filePath = dir,
        recursive = true,
        modes = WatchEventKind.ALL,
      )
      watcher.pollEvents {
        println("Event->${it.file} -> ${it.type}")
      }
    } catch (e: Throwable) {
      e.printStackTrace()
      throw e
    }
  }
}
