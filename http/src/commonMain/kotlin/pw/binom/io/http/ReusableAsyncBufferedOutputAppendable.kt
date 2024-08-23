@file:OptIn(DelicateCoroutinesApi::class)

package pw.binom.io.http

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.AsyncBufferedOutputAppendable
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool

class ReusableAsyncBufferedOutputAppendable(
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  charBufferSize: Int = bufferSize / 2,
  val onClose: (ReusableAsyncBufferedOutputAppendable) -> Unit,
) : AsyncBufferedOutputAppendable(
  charset = Charsets.UTF8,
  output = AsyncOutput.NULL,
  pool = null,
  charBufferSize = charBufferSize,
  closeParent = false,
  buffer = ByteBuffer(bufferSize),
  closeBuffer = true,
) {
  class Manager(
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    val charBufferSize: Int = bufferSize / 2,
  ) : ObjectFactory<ReusableAsyncBufferedOutputAppendable> {

    override fun deallocate(
      value: ReusableAsyncBufferedOutputAppendable,
      pool: ObjectPool<ReusableAsyncBufferedOutputAppendable>,
    ) {
      GlobalScope.launch {
        value.free()
      }
    }

    override fun allocate(pool: ObjectPool<ReusableAsyncBufferedOutputAppendable>): ReusableAsyncBufferedOutputAppendable =
      ReusableAsyncBufferedOutputAppendable(
        bufferSize = bufferSize,
        charBufferSize = charBufferSize,
        onClose = { pool.recycle(it) },
      )
  }

  private val currentCharset = Charsets.UTF8
  fun reset(output: AsyncOutput, charset: Charset) {
    if (currentCharset.name !== charset.name) {
      encoder.close()
      encoder = charset.newEncoder()
    }
    this.output = output
  }

  suspend fun free() {
    super.asyncClose()
  }

  override suspend fun asyncClose() {
    try {
      flush()
      output.asyncClose()
    } finally {
      charBuffer.clear()
      buffer.clear()
      onClose(this)
    }
  }
}
