package pw.binom.db.postgresql.async

import pw.binom.io.AsyncOutput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.writeByte
import pw.binom.writeInt
import pw.binom.writeShort

class PackageWriter(val connection: PGConnection) : Closeable {
    val buf16 = ByteBuffer.alloc(16)
    val output = ByteArrayOutput()
    private var cmdExist = false
    private var bodyStarted = false

    private inline fun checkBodyStarted() {
        check(bodyStarted) { "Body not started" }
    }

    private inline fun checkBodyNotStarted() {
        check(!bodyStarted) { "Body already started" }
    }

    fun startBody() {
        checkBodyNotStarted()
        output.writeInt(buf16, 0)
        bodyStarted = true
    }

    fun writeCmd(cmd: Byte) {
        checkBodyNotStarted()
        if (cmdExist) {
            throw IllegalStateException("Cmd already wrote")
        }
        output.writeByte(buf16, cmd)
        cmdExist = true
    }

    fun endBody() {
        checkBodyStarted()
        val pos = output.data.position
        output.data.position = if (cmdExist) 1 else 0
        val len = if (cmdExist) output.size - 1 else output.size
        output.data.writeInt(buf16, len)
        output.data.position = pos
    }

    suspend fun finishAsync(output: AsyncOutput) {
        output.write(this.output.lock())
        this.output.clear()
        cmdExist = false
        bodyStarted = false
    }

    fun writeCString(text: String) {
        checkBodyStarted()
        connection.charsetUtils.encode(text) {
            output.write(it)
        }
        output.writeByte(buf16, 0)
    }

    fun writeLengthString(text: String) {
        checkBodyStarted()
        val pos = output.data.position
        output.writeInt(buf16, 0)

        connection.charsetUtils.encode(text) {
            output.write(it)
        }

        val pos2 = output.data.position
        output.data.position = pos
        output.data.writeInt(buf16, pos2 - pos - 4)
        output.data.position = pos2
    }

    override fun close() {
        buf16.close()
        output.close()
    }

    fun write(data: ByteArray) {
        checkBodyStarted()
        data.forEach {
            output.writeByte(buf16, it)
        }
    }

    fun writeShort(value: Short) {
        checkBodyStarted()
        output.writeShort(buf16, value)
    }

    fun writeInt(value: Int) {
        checkBodyStarted()
        output.writeInt(buf16, value)
    }

    fun writeByte(value: Byte) {
        checkBodyStarted()
        output.writeByte(buf16, value)
    }
}
