package pw.binom.io.file

import pw.binom.Environment
import pw.binom.concurrency.Worker
import pw.binom.concurrency.execute
import pw.binom.concurrency.sleep
import pw.binom.io.ClosedException
import pw.binom.io.use
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class FileWatcherTest {

    fun afterTime(time: Duration = 1.seconds, func: () -> Unit) {
        val w = Worker()
        w.execute {
            try {
                sleep(time)
                func()
            } finally {
                w.requestTermination()
            }
        }
    }

    private fun withRootDir(func: (File) -> Unit) {
        val root = File.temporalDirectory!!.relative("file-test-${Random.nextUuid()}")
        root.mkdirs()
        try {
            func(root)
        } finally {
            root.deleteRecursive()
        }
    }

    @Test
    fun testNoEvent() {
        withRootDir { root ->
            val currentDir = Environment.workDirectoryFile
            root.mkdirs()
            FileWatcher().use { watcher ->
                watcher.watch(root, create = true, modify = true, delete = true).use { listener ->
                    assertEquals(
                        0,
                        watcher.pullChanges(5.seconds) {
                            fail()
                        }
                    )
                }
            }
            println("dir: $currentDir")
        }
    }

    @Test
    fun testEvents() {
        withRootDir { root ->
            val currentDir = Environment.workDirectoryFile
            root.mkdirs()
            FileWatcher().use { watcher ->
                val newFile = root.relative("ololo")
                afterTime { newFile.mkdirs() }
                watcher.watch(root, create = true, modify = true, delete = true).use { listener ->
                    assertEquals(
                        1,
                        watcher.pullChanges(5.seconds) {
                            assertEquals(newFile, it.file)
                            assertEquals(ChangeType.CREATE, it.type)
                        }
                    )
                    afterTime { newFile.relative("gggg").mkdirs() }
                    assertEquals(
                        0,
                        watcher.pullChanges(5.seconds) {
                            fail()
                        }
                    )
                }
            }
        }
    }

    @Test
    fun closeTest() {
        val watcher = FileWatcher()
        watcher.close()
        try {
            watcher.close()
            fail("Watcher should throw ClosedException")
        } catch (e: ClosedException) {
            // ok
        }
    }
}
