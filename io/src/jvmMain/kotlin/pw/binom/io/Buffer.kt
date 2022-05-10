package pw.binom.io

actual interface Buffer {
    actual val remaining123: Int
    actual var position: Int
    actual var limit: Int
    actual val capacity: Int
    actual fun flip()
    actual fun compact()
    actual fun clear()
    actual val elementSizeInBytes: Int
}
