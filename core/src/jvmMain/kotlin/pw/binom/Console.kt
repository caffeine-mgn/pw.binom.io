package pw.binom

import pw.binom.io.AppendableUTF8
import pw.binom.io.Reader
import pw.binom.io.ReaderUTF82
import java.io.PrintStream
import java.nio.channels.Channels

//private val tmpBuf = ByteBuffer.alloc(32)

actual object Console {
    private class Out(oo: PrintStream) : Output {
        val vv = Channels.newChannel(oo)
        override fun close() {
        }

        override fun write(data: ByteBuffer): Int =
            vv.write(data.native)

        override fun flush() {
        }

    }

    actual val stdChannel: Output = Out(java.lang.System.out)
    actual val errChannel: Output = Out(java.lang.System.err)

    actual val inChannel: Input = object : Input {
        val cc = Channels.newChannel(java.lang.System.`in`)
//        override fun skip(length: Long): Long {
//            var l = length
//            while (l > 0) {
//                tmpBuf.reset(0, minOf(tmpBuf.capacity, l.toInt()))
//                l -= read(tmpBuf)
//            }
//            return length
//        }

        override fun read(dest: ByteBuffer): Int =
            cc.read(dest.native)

//        override fun read(data: ByteDataBuffer, offset: Int, length: Int) =
//                data.update(offset, length) {
//                    cc.read(it)
//                }

        override fun close() {
        }

    }
    actual val std: Appendable = AppendableUTF8(stdChannel)
    actual val err: Appendable = AppendableUTF8(errChannel)
    actual val input: Reader = ReaderUTF82(inChannel)

}