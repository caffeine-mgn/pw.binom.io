package pw.binom.db.postgresql.async

import pw.binom.asyncOutput
import pw.binom.charset.Charset
import pw.binom.collections.defaultMutableList
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.*
import pw.binom.io.*
import pw.binom.readByteArray

class PackageReader(
    val connection: PGConnection,
    val charset: Charset,
    private val temporalBuffer: ByteBuffer,
    private val rawInput: AsyncBufferedAsciiInputReader
) : Closeable {
    val authenticationChallengeMessage = AuthenticationMessage.AuthenticationChallengeMessage()
    val errorMessage = ErrorMessage()
    val noticeMessage = NoticeMessage()
    val queryMessage = QueryMessage()
    val notificationResponseMessage = NotificationResponseMessage()
    val processData = ProcessData()
    val commandCompleteMessage = CommandCompleteMessage()
    val readyForQueryMessage = ReadyForQueryMessage()
    val parameterStatusMessage = ParameterStatusMessage()
    val rowDescriptionMessage = RowDescriptionMessage()
    val data = QueryResponse.Data(connection)
    val preparedStatementOpeningMessage = PreparedStatementOpeningMessage()
    val dataRowMessage = DataRowMessage()
    val bindMessage = BindMessage()
    val executeMessage = ExecuteMessage()
    val describeMessage = DescribeMessage()
    val closeMessage = CloseMessage()

    private val output = ByteArrayOutput()
    private val stringBuffer = ByteArrayOutput()
    private val stringBufferAsync = stringBuffer.asyncOutput()
    private val limitInput = AsyncInputLimit(rawInput)
    private val columns = defaultMutableList<ColumnMeta>()
    private var columnIndex = 0

    fun recycleColumnData(meta: ColumnMeta) {
        // Do nothing
    }

    fun giveColumnData(): ColumnMeta {
        return ColumnMeta()
//        if (columnIndex >= columns.size) {
//            columnIndex++
//            val r = ColumnMeta()
//            columns.add(r)
//            return r
//        }
//        val r = columns[columnIndex]
//        columnIndex++
//        return r
    }

    val input: AsyncInput
        get() = limitInput

    var length = 0
        private set

    val remaining
        get() = limitInput.limit
    private var bodyReading = false

    fun end() {
        check(bodyReading) { "Body not started" }
        check(remaining <= 0) { "Body read not all. remaining: [$remaining]" }
        bodyReading = false
    }

    fun startBody(length: Int) {
        check(!bodyReading) { "Body already started" }
        this.length = length
        limitInput.limit = length
        output.clear()
        columnIndex = 0
        bodyReading = true
    }

    override fun close() {
        stringBuffer.close()
        output.close()
    }

    suspend fun readCString(): String {
        if (bodyReading) {
            limitInput.readUntil(byte = 0.toByte(), exclude = true, dest = stringBufferAsync)
        } else {
            rawInput.readUntil(byte = 0.toByte(), exclude = true, dest = stringBufferAsync)
        }
//        while (true) {
//            val byte = readByte()
//            if (byte == 0.toByte()) {
//                break
//            }
//            stringBuffer.writeByte(byte)
//        }
        if (stringBuffer.size <= 0) {
            return ""
        }
        val data = stringBuffer.locked {
            connection.charsetUtils.decode(it)
        }
        this.stringBuffer.clear()
        return data
    }

    suspend fun readByte() = if (bodyReading) limitInput.readByte() else rawInput.readByte()
    suspend fun readShort() =
        if (bodyReading) limitInput.readShort() else rawInput.readShort()

    suspend fun readInt() = if (bodyReading) limitInput.readInt() else rawInput.readInt()
    suspend fun readByteArray(length: Int): ByteArray = if (bodyReading) {
        limitInput.readByteArray(size = length, buffer = temporalBuffer)
    } else {
        rawInput.readByteArray(size = length, buffer = temporalBuffer)
    }
}

private class AsyncInputLimit(val input: AsyncBufferedAsciiInputReader) : AsyncInput {
    var limit = 0

    suspend fun readUntil(byte: Byte, exclude: Boolean, dest: AsyncOutput): Int {
        val r = input.readUntil(
            byte = byte,
            exclude = exclude,
            dest = dest
        )
        limit -= r
        return r
    }

    override val available: Int
        get() = if (input.available > 0) {
            minOf(input.available, limit)
        } else {
            input.available
        }

    suspend fun readByte(): Byte {
        if (limit < Byte.SIZE_BYTES) {
            throw EOFException()
        }
        val value = input.readByte()
        limit -= Byte.SIZE_BYTES
        return value
    }

    suspend fun readShort(): Short {
        if (limit < Short.SIZE_BYTES) {
            throw EOFException()
        }
        val value = input.readShort()
        limit -= Short.SIZE_BYTES
        return value
    }

    suspend fun readInt(): Int {
        if (limit < Int.SIZE_BYTES) {
            throw EOFException()
        }
        val value = input.readInt()
        limit -= Int.SIZE_BYTES
        return value
    }

    suspend fun readLong(): Long {
        if (limit < Long.SIZE_BYTES) {
            throw EOFException()
        }
        val value = input.readLong()
        limit -= Long.SIZE_BYTES
        return value
    }

    override suspend fun read(dest: ByteBuffer): Int {
        val limit = minOf(dest.remaining, limit)
        val l = dest.limit
        dest.limit = dest.position + limit
        val read = input.read(dest)
        this.limit -= read
        dest.limit = l
        return read
    }

    override suspend fun asyncClose() {
        input.asyncClose()
    }
}
