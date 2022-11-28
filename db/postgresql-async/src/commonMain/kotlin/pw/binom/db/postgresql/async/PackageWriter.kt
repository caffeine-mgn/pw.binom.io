package pw.binom.db.postgresql.async

import pw.binom.io.AsyncOutput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.uuid.UUID
import pw.binom.writeInt

class PackageWriter(val connection: PGConnection, val temporalBuffer: ByteBuffer) : Closeable {
    val buf16 = ByteBuffer.alloc(16)
    val output = ByteArrayOutput()
    private var cmdExist = false
    private var bodyStarted = false

    private fun checkBodyStarted() {
        check(bodyStarted) { "Body not started" }
    }

    private fun checkBodyNotStarted() {
        check(!bodyStarted) { "Body already started" }
    }

    fun startBody() {
        checkBodyNotStarted()
        output.writeInt(0)
        bodyStarted = true
    }

    fun writeCmd(cmd: Byte) {
        checkBodyNotStarted()
        check(!cmdExist) { "Cmd already wrote" }
        output.writeByte(cmd)
        cmdExist = true
    }

    fun endBody() {
        checkBodyStarted()
        val pos = output.data.position
        output.data.position = if (cmdExist) 1 else 0
        val len = if (cmdExist) output.size - 1 else output.size
        output.data.writeInt(len)
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
        output.writeByte(0)
    }

    fun writeLengthString(text: String) {
        checkBodyStarted()
        val pos = output.data.position
        output.writeInt(0)

        connection.charsetUtils.encode(text) {
            output.write(it)
        }

        val pos2 = output.data.position
        output.data.position = pos
        output.data.writeInt(pos2 - pos - 4)
        output.data.position = pos2
    }

    override fun close() {
        buf16.close()
        output.close()
    }

    fun write(data: ByteArray) {
        checkBodyStarted()
        output.write(data)
    }

    fun writeShort(value: Short) {
        checkBodyStarted()
        output.writeShort(value)
    }

    fun writeInt(value: Int) {
        checkBodyStarted()
        output.writeInt(value)
    }

    fun writeUUID(value: UUID) {
        checkBodyStarted()
        output.writeLong(value.mostSigBits)
        output.writeLong(value.leastSigBits)
    }

    fun writeByte(value: Byte) {
        checkBodyStarted()
        output.writeByte(value)
    }
}
