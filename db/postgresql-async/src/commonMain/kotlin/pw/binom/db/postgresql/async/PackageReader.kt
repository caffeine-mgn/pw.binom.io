package pw.binom.db.postgresql.async

import pw.binom.charset.Charset
import pw.binom.collections.defaultMutableList
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.*
import pw.binom.io.AsyncInput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.readByte
import pw.binom.writeByte

class PackageReader(val connection: PGConnection, val charset: Charset, val rawInput: AsyncInput) : Closeable {
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

    val buf16 = ByteBuffer.alloc(16)
    private val output = ByteArrayOutput()
    private val stringBuffer = ByteArrayOutput()
    private val limitInput = AsyncInputLimit(rawInput)
    private val columns = defaultMutableList<ColumnMeta>()
    private var columnIndex = 0

    fun giveColumnData(): ColumnMeta {
        if (columnIndex >= columns.size) {
            columnIndex++
            val r = ColumnMeta()
            columns.add(r)
            return r
        }
        val r = columns[columnIndex]
        columnIndex++
        return r
    }

    val input: AsyncInput
        get() = limitInput

    var length = 0
        private set

    val remaining
        get() = limitInput.limit

    fun end() {
        if (remaining > 0) {
            throw IllegalStateException("Body read not all. remaining: [$remaining]")
        }
    }

    fun startBody(length: Int) {
        this.length = length
        limitInput.limit = length
        output.clear()
        columnIndex = 0
    }

    override fun close() {
        stringBuffer.close()
        buf16.close()
        output.close()
    }

    suspend fun readCString(): String {
        while (true) {
            val byte = input.readByte(buf16)
            if (byte == 0.toByte()) {
                break
            }
            stringBuffer.writeByte(buf16, byte)
        }
        if (stringBuffer.size <= 0) {
            return ""
        }
        val data = stringBuffer.locked {
            connection.charsetUtils.decode(it)
        }
        this.stringBuffer.clear()
        return data
    }

    suspend fun readByteArray(length: Int): ByteArray {
        val out = ByteArray(length) {
            input.readByte(buf16)
        }
        return out
    }
}

private class AsyncInputLimit(val input: AsyncInput) : AsyncInput {
    var limit = 0
    override val available: Int
        get() = if (input.available > 0) {
            minOf(input.available, limit)
        } else {
            input.available
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
