package pw.binom.io.file

interface WatchEvent {
    val file: File
    val type: WatchEventKind
}
