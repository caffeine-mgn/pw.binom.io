package pw.binom

import pw.binom.io.Input
import pw.binom.io.Output
import pw.binom.io.Reader
import pw.binom.io.Writer

actual object Console {
    actual val stdChannel: Output = TODO()
    actual val errChannel: Output = TODO()
    actual val inChannel: Input = TODO()

    actual val std: Writer = TODO()
    actual val err: Writer = TODO()
    actual val input: Reader = TODO()
}
