package pw.binom.io

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
          input.asyncClose()
          output.asyncClose()
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
