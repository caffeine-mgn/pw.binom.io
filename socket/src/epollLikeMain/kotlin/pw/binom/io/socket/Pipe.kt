package pw.binom.io.socket

internal expect fun createPipe(): Pair<Int, Int>
internal expect fun closePipe(value: Int)
