package pw.binom.io

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AsyncBufferedAsciiInputReaderTest {
    @Test
    fun readText() = runTest {
        val ttt =
            byteArrayOf(115, 116, 97, 114, 116)
                .wrap().asAsyncChannel()
                .bufferedAsciiReader()
                .use { it.readText() }
        println("Result $ttt")
    }
}
