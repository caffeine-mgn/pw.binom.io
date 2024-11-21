package pw.binom.io

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pw.binom.InternalLog
import pw.binom.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface AsyncChannel : AsyncCloseable, AsyncOutput, AsyncInput {
  companion object {

    val EMPTY = create(
      input = AsyncInput.EMPTY,
      output = AsyncOutput.NULL,
    )

    fun create(input: AsyncInput, output: AsyncOutput): AsyncChannel {
      if (input === output && input is AsyncChannel) {
        return input
      }
      return object : AsyncChannel {
        override suspend fun asyncClose() {
          IoDetectSlow("AsyncChannel.create.asyncClose #0 input=${input::class} ($input), output=${output::class} ($output)") {
            input.asyncClose()
          }
          IoDetectSlow("AsyncChannel.create.asyncClose #1 input=${input::class} ($input), output=${output::class} ($output)") {
            output.asyncClose()
          }
        }

        override suspend fun flush() {
          IoDetectSlow("AsyncChannel.create.flush #0 input=${input::class} ($input), output=${output::class} ($output)") {
            output.flush()
          }
        }

        override val available: Int
          get() = input.available

        override suspend fun read(dest: ByteBuffer) =
          input.read(dest)

        override suspend fun write(data: ByteBuffer) =
          IoDetectSlow("AsyncChannel.create.write #0 input=${input::class} ($input), output=${output::class} ($output)") {
            output.write(data)
          }

        override fun toString(): String = "AsyncChannel(input=$input, output=$output)"
      }
    }

    fun create(input: AsyncInput, output: AsyncOutput, onClose: suspend () -> Unit) = object : AsyncChannel {
      override suspend fun asyncClose() {
        onClose()
      }

      override suspend fun flush() {
        output.flush()
      }

      override val available: Int
        get() = input.available

      override suspend fun read(dest: ByteBuffer) =
        input.read(dest)

      override suspend fun write(data: ByteBuffer) =
        output.write(data)

      override fun toString(): String = "AsyncChannel($input, $output)"
    }

    fun <T : AsyncChannel> create(channel: T, onClose: suspend (T) -> Unit) = object : AsyncChannel {
      override suspend fun asyncClose() {
        onClose(channel)
      }

      override suspend fun flush() {
        channel.flush()
      }

      override val available: Int
        get() = channel.available

      override suspend fun read(dest: ByteBuffer) =
        channel.read(dest)

      override suspend fun write(data: ByteBuffer) =
        channel.write(data)

      override fun toString(): String = "AsyncChannel($channel)"
    }
  }
}

inline fun <T> IoDetectSlow(msg: String, duration: Duration = 1.seconds, func: () -> T): T {
  val stackTrace = Throwable()
  val finished = AtomicBoolean(false)
  GlobalScope.launch {
    delay(duration)
    if (!finished.getValue()) {
      InternalLog.warn(file = "IO") { "Slow: $msg\n${stackTrace.stackTraceToString()}" }
      println("IO---->Slow: $msg\n${stackTrace.stackTraceToString()}")
    }
  }

  return try {
    func()
  } finally {
    finished.setValue(true)
  }
}
