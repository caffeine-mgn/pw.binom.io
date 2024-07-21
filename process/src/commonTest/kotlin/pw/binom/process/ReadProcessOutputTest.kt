package pw.binom.process

import pw.binom.io.*
import pw.binom.thread.Thread
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class ReadProcessOutputTest {
  @Test
  fun test() {
    val starter = ProcessStarter.create("/bin/pwd")
    Thread {
      try {
        Thread.sleep(1000)
        ByteBuffer(100).use { buffer ->
          val text = starter.stdout.bufferedAsciiReader().use {
            it.readText()
          }
          println(text)
        }
      } catch (e: Throwable) {
        e.printStackTrace()
      }
    }.start()
    val process = starter.start()
    process.join()
    Thread.sleep(3000)
  }
}
