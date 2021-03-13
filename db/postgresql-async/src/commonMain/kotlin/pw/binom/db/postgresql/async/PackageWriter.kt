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
    var bodyStarted = false

    fun startBody() {
        check(!bodyStarted)
        output.writeInt(buf16, 0)
        bodyStarted = true
    }
private var lastCmd:Byte?=null
    fun writeCmd(cmd: Byte) {
        println("writeCmd(cmd: $cmd)")
        check(!bodyStarted)
        if (cmdExist) {
            throw IllegalStateException("Cmd already wrote")
        }
        output.writeByte(buf16, cmd)
        cmdExist = true
        lastCmd=cmd
    }

    fun endBody() {
        println("endBody() cmd: $lastCmd")
        check(bodyStarted)
        val pos = output.data.position
        output.data.position = if (cmdExist) 1 else 0
        val len = if (cmdExist) output.size - 1 else output.size
        output.data.writeInt(buf16, len)
        output.data.position = pos
    }

    suspend fun finishAsync(output: AsyncOutput) {
        println("finishAsync() cmd: $lastCmd")
        this.output.data.flip()
        output.write(this.output.data)
        this.output.clear()
        cmdExist = false
        bodyStarted = false
    }

    fun writeCString(text: String) {
        check(bodyStarted)
        appender.append(text)
        appender.flush()
        output.writeByte(buf16, 0)
    }

    fun writeLengthString(text: String) {
        check(bodyStarted)
        val pos = output.data.position
        output.writeInt(buf16, 0)

        appender.append(text)
        appender.flush()

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
        check(bodyStarted)
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
        check(bodyStarted)
        output.writeShort(buf16, value)
    }

    fun writeInt(value: Int) {
        check(bodyStarted)
        output.writeInt(buf16, value)
    }

    fun writeByte(value: Byte) {
        check(bodyStarted)
        output.writeByte(buf16, value)
    }

}