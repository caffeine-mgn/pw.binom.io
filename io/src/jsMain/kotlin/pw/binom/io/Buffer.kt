package pw.binom.io

actual interface Buffer {
    actual var position: Int
    actual var limit: Int
    actual val remaining: Int
    actual val capacity: Int
    actual val elementSizeInBytes: Int
    actual fun flip()
    actual fun compact()
    actual fun clear()
}
