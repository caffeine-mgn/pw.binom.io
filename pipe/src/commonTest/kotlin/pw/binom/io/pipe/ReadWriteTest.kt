package pw.binom.io.pipe

import pw.binom.concurrency.SpinLock
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.use
import pw.binom.io.wrap
import pw.binom.thread.Thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ReadWriteTest {
  @Test
  fun readWrite() {
    PipeInput().use { input ->
      PipeOutput(input).use { output ->
        byteArrayOf(1, 2, 3).wrap {
          output.write(it)
        }
        ByteBuffer(10).use { buffer ->
          assertEquals(3, input.read(buffer).length)
          buffer.flip()
          assertEquals(1, buffer[0])
          assertEquals(2, buffer[1])
          assertEquals(3, buffer[2])
        }
      }
    }
  }

  @Test
  fun blockingReadTest() {
    PipeInput().use { input ->
      PipeOutput(input).use { output ->
        Thread {
          Thread.sleep(1.seconds)
          byteArrayOf(1, 2, 3).wrap {
            output.write(it)
          }
        }.start()
        val readingTime = measureTime {
          ByteBuffer(10).use { buffer ->
            assertEquals(3, input.read(buffer).length)
            buffer.flip()
            assertEquals(1, buffer[0])
            assertEquals(2, buffer[1])
            assertEquals(3, buffer[2])
          }
        }
        assertTrue(readingTime > 1.seconds, "Reading time less then 1 second: $readingTime")
        assertTrue(readingTime < 4.seconds, "Reading time more then 4 seconds: $readingTime")
      }
    }
  }

  @Test
  fun closePipeOnRead() {
    PipeInput().use { input ->
      PipeOutput(input).use { output ->
        Thread {
          Thread.sleep(1.seconds)
          output.close()
        }.start()
        val readingTime = measureTime {
          ByteBuffer(10).use { buffer ->
            val r = input.read(buffer)
            assertEquals(DataTransferSize.CLOSED, r)
          }
        }
        assertTrue(readingTime > 1.seconds, "Reading time less then 1 second: $readingTime")
        assertTrue(readingTime < 4.seconds, "Reading time more then 4 seconds: $readingTime")
      }
    }
  }

  @Test
  fun closePipeOnWrite() {
    val l = SpinLock()
    l.lock()
    PipeInput().use { input ->
      PipeOutput(input).use { output ->
        Thread {
          ByteBuffer(3).use { buffer ->
            input.read(buffer)
          }
          Thread.sleep(1.seconds)
          output.close()
          l.unlock()

        }.start()
        val readingTime = measureTime {
          val r1 = byteArrayOf(1, 2, 3).wrap { buffer ->
            output.write(buffer)
          }
          l.lock()
          assertEquals(3, r1.length)
          val r2 = byteArrayOf(1, 2, 3).wrap { buffer ->
            output.write(buffer)
          }
          assertEquals(DataTransferSize.CLOSED, r2)
        }
        assertTrue(readingTime > 1.seconds, "Reading time less then 1 second: $readingTime")
        assertTrue(readingTime < 4.seconds, "Reading time more then 4 seconds: $readingTime")
      }
    }
  }
}
