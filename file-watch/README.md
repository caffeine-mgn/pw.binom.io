# Library for watching changes on file system

# Usage
```kotlin
import java.io.File

val watcher = FileWatcher.createDefault()
watcher.register(
    filePath = File("/home/user/files"),
    recursive = true,
    modes = WatchEventKind.CREATE + WatchEventKind.MODIFY,
)
var counter = 100
while (counter > 0) {
    counter--
    watcher.pollEvents { event ->
        if (event.isModify) {
            println("File ${event.file} ${event.type}")
        }
    }
}
watcher.close()

```
