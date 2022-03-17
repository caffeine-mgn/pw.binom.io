package pw.binom.db.radis

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncChannel
import pw.binom.io.ClosedException
import pw.binom.io.bufferedReader
import pw.binom.io.bufferedWriter

class RadisConnectionImpl(val connection: AsyncChannel) : RadisConnection {
    private val reader = connection.bufferedReader(closeParent = false)
    private val writer = connection.bufferedWriter(closeParent = false)

    suspend fun ping() {
        checkClosed()
        writer.append("PING\r\n")
        writer.flush()
        println("send ping")
        if (readResponse() != "PONG") {
            throw IllegalStateException("Invalid pong response")
        }
    }

    private val closed = AtomicBoolean(false)
    private fun checkClosed() {
        if (closed.value) {
            throw ClosedException()
        }
    }

    override suspend fun asyncClose() {
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        connection.asyncClose()
    }

    private suspend fun readResponse(): Any? {
        println("reading response")
        val char = reader.readChar() ?: return null

        when (char) {
            '+' -> return reader.readln() // Simple String
            '-' -> throw RadisException(reader.readln()) // Error
            '$' -> { // String Bulk
                val len = reader.readln()?.toIntOrNull() ?: throw IllegalStateException("Invalid String Bulk")
                if (len < 0) {
                    return null
                }
                val str = reader.readUntil({ _, sb -> sb.length != len })
                reader.readln()
                str
            }
            ':' -> { // Long
                val num = reader.readln() ?: throw IllegalStateException("Invalid Integer")
                if (num == "_") {
                    return null
                }
                return num.toLongOrNull() ?: throw IllegalStateException("Invalid Integer \"$num\"")
            }
            ',' -> { // Double
                val num = reader.readln() ?: throw IllegalStateException("Invalid Decimal")
                return when (num) {
                    "_" -> null
                    "inf" -> Double.POSITIVE_INFINITY
                    "-inf" -> Double.NEGATIVE_INFINITY
                    else -> num.toDoubleOrNull() ?: throw IllegalStateException("Invalid Decimal \"$num\"")
                }
            }
            '#' -> { // Boolean
                return when (val len = reader.readln()) {
                    "t" -> true
                    "f" -> false
                    else -> throw IllegalStateException("Invalid Boolean \"$len\"")
                }
            }
            '*' -> { // List
                val len = reader.readln()?.toIntOrNull() ?: throw IllegalStateException("Invalid String Bulk")
                if (len < 0) { // TODO обработать бесконечный список
                    return null
                }
            }
        }

        val line = reader.readln() ?: return null
        if (line.startsWith("+")) {
            return line.substring(1)
        }
        if (line.startsWith("")) {
            throw RadisException(line.substring(1))
        }
        throw RuntimeException("Unknown response \"$line\"")
    }
}
