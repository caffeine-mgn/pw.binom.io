package pw.binom.db.postgresql.async

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.io.BufferedOutputAppendable
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Closeable

class PackageWriter(val charset: Charset, longPool: ByteBufferPool) : Closeable {
    val buf16 = ByteBuffer.alloc(16)
    val output = ByteArrayOutput()
    val appender = BufferedOutputAppendable(charset, output, longPool)
    private var cmdExist = false

    fun startBody() {
        output.writeInt(buf16, 0)
    }

    fun writeCmd(cmd: Byte) {
        if (cmdExist) {
            throw IllegalStateException("Cmd already wrote")
        }
        output.writeByte(buf16, cmd)
        cmdExist = true
    }

    fun rewriteSize() {
        val pos = output.data.position
        output.data.position = if (cmdExist) 1 else 0
        val len = if (cmdExist) output.size - 1 else output.size
        output.data.writeInt(buf16, len)
        output.data.position = pos
    }

    suspend fun finishAsync(output: AsyncOutput) {
        this.output.data.flip()
        output.write(this.output.data)
        this.output.clear()
        cmdExist = false
    }

    fun writeCString(text: String) {
        appender.append(text)
        appender.flush()
        output.writeByte(buf16, 0)
    }

    override fun close() {
        buf16.close()
        output.close()
    }

    fun write(data: ByteArray) {

        data.forEach {
            output.writeByte(buf16, it)
        }

//        var l = data.size
//        while (l > 0) {
//            buf16.position = 0
//            buf16.limit = minOf(l, buf16.capacity)
//            buf16.write(data, data.size - l, buf16.limit)
//            buf16.flip()
//            l -= output.write(buf16)
//        }
    }

    fun writeShort(value: Short) {
        output.writeShort(buf16, value)
    }

}