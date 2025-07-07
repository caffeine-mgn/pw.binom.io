package pw.binom

import pw.binom.io.Input
import pw.binom.io.Output
import pw.binom.io.Reader
import pw.binom.io.Writer

actual object Console {
    actual val stdChannel: Output
        get() = TODO("Not yet implemented")
    actual val errChannel: Output
        get() = TODO("Not yet implemented")
    actual val inChannel: Input
        get() = TODO("Not yet implemented")
    actual val std: Writer
        get() = TODO("Not yet implemented")
    actual val err: Writer
        get() = TODO("Not yet implemented")
    actual val input: Reader
        get() = TODO("Not yet implemented")
}
