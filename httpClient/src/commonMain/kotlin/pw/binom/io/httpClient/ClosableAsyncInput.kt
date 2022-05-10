package pw.binom.io.httpClient

// /**
// * Stream for read http response. This stream is close when this stream can't read data from [stream]
// */
// @Deprecated(message = "Use HttpClient", level = DeprecationLevel.WARNING)
// internal class ClosableAsyncInput(val stream: AsyncInput) : AsyncHttpInput {
//    private var eof = false
//    override val isEof: Boolean
//        get() = eof
//
//    override val available: Int
//        get() = if (eof) 0 else stream.available
//
//    override suspend fun read(dest: ByteBuffer): Int =
//        try {
//            val r = stream.read(dest)
//            if (r == 0) {
//                asyncClose()
//            }
//            r
//        } catch (e: IOException) {
//            asyncClose()
//            0
//        }
//
//    override suspend fun asyncClose() {
//        eof = true
//    }
//
// }
