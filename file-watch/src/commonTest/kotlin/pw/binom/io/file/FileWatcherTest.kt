package pw.binom.io.file

import kotlin.test.Test

class FileWatcherTest {
    @Test
    fun aa() {
        val watcher = FileWatcher.createDefault()
        val e = watcher.register(
            filePath = File("/tmp/gg/vv/fff"),
            recursive = true,
            modes = WatchEventKind.ALL,
        )
        watcher.pollEvents {
            println("Event->${it.file} -> ${it.type}")
        }
    }
}
