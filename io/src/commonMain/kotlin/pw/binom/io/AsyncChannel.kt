package pw.binom.io

interface AsyncChannel : AsyncCloseable, AsyncOutput, AsyncInput {
    companion object {
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

                override suspend fun read(dest: ByteBuffer): Int =
                    input.read(dest)

                override suspend fun write(data: ByteBuffer): Int =
                    output.write(data)
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

            override suspend fun read(dest: ByteBuffer): Int =
                input.read(dest)

            override suspend fun write(data: ByteBuffer): Int =
                output.write(data)
        }
    }
}
