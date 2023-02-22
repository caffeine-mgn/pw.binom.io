package pw.binom.charset

import kotlinx.coroutines.test.runTest
import pw.binom.asyncOutput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBufferFactory
import pw.binom.io.bufferedWriter
import pw.binom.io.use
import pw.binom.pool.GenericObjectPool
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncBufferedOutputAppendableTest {
    val txt = """# HELP binom_byte_buffer_count ByteBuffer Count
# TYPE binom_byte_buffer_count gauge
binom_byte_buffer_count 4
# HELP binom_byte_buffer_memory ByteBuffer Memory
# TYPE binom_byte_buffer_memory gauge
binom_byte_buffer_memory 1124
# HELP binom_charset_encoder_count Charset Encoder Count
# TYPE binom_charset_encoder_count gauge
binom_charset_encoder_count 0
# HELP binom_charset_decoder_count Charset Decoder Count
# TYPE binom_charset_decoder_count gauge
binom_charset_decoder_count 0
# HELP binom_base_http_client BaseHttpClient Count
# TYPE binom_base_http_client gauge
binom_base_http_client 1
# HELP binom_default_http_request DefaultHttpRequest Count
# TYPE binom_default_http_request gauge
binom_default_http_request 0
# HELP binom_thread_count Thread Count
# TYPE binom_thread_count gauge
binom_thread_count 8
# HELP binom_selector_key_count SelectorKey Count
# TYPE binom_selector_key_count gauge
binom_selector_key_count 3
# HELP binom_selector_key_alloc_count SelectorKey Alloc Count
# TYPE binom_selector_key_alloc_count gauge
binom_selector_key_alloc_count 3"""

    @Test
    fun withPool() = runTest {
        val pool = GenericObjectPool(initCapacity = 0, factory = ByteBufferFactory(size = 50))
        val data = ByteArrayOutput()
        val out = data.asyncOutput()
        val buffer = out.bufferedWriter(closeParent = false, pool = pool)
        buffer.use { it.append(txt) }
        assertEquals(txt, data.toByteArray().decodeToString())
    }

    @Test
    fun withoutPool() = runTest {
        val data = ByteArrayOutput()
        val out = data.asyncOutput()
        val buffer = out.bufferedWriter(closeParent = false, bufferSize = 50)
        buffer.use { it.append(txt) }
        assertEquals(txt, data.toByteArray().decodeToString())
    }
}
