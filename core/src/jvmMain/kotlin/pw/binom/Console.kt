package pw.binom

import pw.binom.io.AppendableUTF82
import pw.binom.io.Reader
import pw.binom.io.ReaderUTF82
import java.io.PrintStream
import java.nio.channels.Channels

private val tmpBuf = ByteDataBuffer.alloc(32)

actual object Console {
    private class Out(oo: PrintStream) : Output {
        val vv = Channels.newChannel(oo)
        override fun close() {
        }

        override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
                data.update(offset, length) {
                    vv.write(it)
                }

        override fun flush() {
        }

    }

    actual val stdChannel: Output = Out(System.out)
    actual val errChannel: Output = Out(System.err)

    actual val inChannel: Input = object : Input {
        val cc = Channels.newChannel(System.`in`)
        override fun skip(length: Long): Long {
            var l = length
            while (l > 0) {
                l -= read(tmpBuf, 0, minOf(tmpBuf.size, l.toInt()))
            }
            return length
        }

        override fun read(data: ByteDataBuffer, offset: Int, length: Int) =
                data.update(offset, length) {
                    cc.read(it)
                }

        override fun close() {
        }

    }
    actual val std: Appendable = AppendableUTF82(stdChannel)
    actual val err: Appendable = AppendableUTF82(errChannel)
    actual val input: Reader = ReaderUTF82(inChannel)

}