package pw.binom.io.file

import pw.binom.thread.Thread
import pw.binom.uuid.nextUuid
import kotlin.random.Random
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
      Thread {
        Thread.sleep(1000)
        dir.relative("123").rewrite("test")
      }.start()
      watcher.pollEvents {
        println("Event->${it.file} -> ${it.type}")
      }
    } catch (e: Throwable) {
      e.printStackTrace()
      throw e
    }
  }
}
