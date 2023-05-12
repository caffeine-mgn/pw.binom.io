package pw.binom.dns

import pw.binom.io.*
import pw.binom.readShort
import pw.binom.writeShort

class PayloadCounters {
    /**
     * number of question entries
     */
    var qCount: UShort = 0u

    /**
     * number of answer entries
     */
    var ansCount: UShort = 0u

    /**
     * number of authority entries
     */
    var authCount: UShort = 0u

    /**
     * number of resource entries
     */
    var addCount: UShort = 0u

    fun read(buffer: ByteBuffer) {
        read(
            buffer = buffer,
            qCount = { qCount = it },
            ansCount = { ansCount = it },
            authCount = { authCount = it },
            addCount = { addCount = it },
        )
    }

    suspend fun read(input: AsyncInput, buffer: ByteBuffer) {
        read(
            input = input,
            buffer = buffer,
            qCount = { qCount = it },
            ansCount = { ansCount = it },
            authCount = { authCount = it },
            addCount = { addCount = it },
        )
    }

    fun read(input: Input, buffer: ByteBuffer) {
        read(
            input = input,
            buffer = buffer,
            qCount = { qCount = it },
            ansCount = { ansCount = it },
            authCount = { authCount = it },
            addCount = { addCount = it },
        )
    }

    fun write(buffer: ByteBuffer) {
        write(
            buffer = buffer,
            qCount = qCount,
            ansCount = ansCount,
            authCount = authCount,
            addCount = addCount,
        )
    }

    companion object {
        const val SIZE_BYTES = Short.SIZE_BYTES * 4
        fun read(buffer: ByteBuffer): PayloadCounters {
            val counters = PayloadCounters()
            counters.read(buffer)
            return counters
        }

        inline fun read(
            buffer: ByteBuffer,
            qCount: (UShort) -> Unit,
            ansCount: (UShort) -> Unit,
            authCount: (UShort) -> Unit,
            addCount: (UShort) -> Unit,
        ) {
            qCount(buffer.readShort().toUShort())
            ansCount(buffer.readShort().toUShort())
            authCount(buffer.readShort().toUShort())
            addCount(buffer.readShort().toUShort())
        }

        suspend inline fun read(
            input: AsyncInput,
            buffer: ByteBuffer,
            qCount: (UShort) -> Unit,
            ansCount: (UShort) -> Unit,
            authCount: (UShort) -> Unit,
            addCount: (UShort) -> Unit,
        ) {
            qCount(input.readShort(buffer).toUShort())
            ansCount(input.readShort(buffer).toUShort())
            authCount(input.readShort(buffer).toUShort())
            addCount(input.readShort(buffer).toUShort())
        }

        inline fun read(
            input: Input,
            buffer: ByteBuffer,
            qCount: (UShort) -> Unit,
            ansCount: (UShort) -> Unit,
            authCount: (UShort) -> Unit,
            addCount: (UShort) -> Unit,
        ) {
            qCount(input.readShort(buffer).toUShort())
            ansCount(input.readShort(buffer).toUShort())
            authCount(input.readShort(buffer).toUShort())
            addCount(input.readShort(buffer).toUShort())
        }

        fun write(qCount: UShort, ansCount: UShort, authCount: UShort, addCount: UShort, buffer: ByteBuffer) {
            buffer.writeShort(qCount.toShort())
            buffer.writeShort(ansCount.toShort())
            buffer.writeShort(authCount.toShort())
            buffer.writeShort(addCount.toShort())
        }

        suspend fun write(
            qCount: UShort,
            ansCount: UShort,
            authCount: UShort,
            addCount: UShort,
            output: AsyncOutput,
            buffer: ByteBuffer,
        ) {
            output.writeShort(value = qCount.toShort(), buffer = buffer)
            output.writeShort(value = ansCount.toShort(), buffer = buffer)
            output.writeShort(value = authCount.toShort(), buffer = buffer)
            output.writeShort(value = addCount.toShort(), buffer = buffer)
        }

        fun write(
            qCount: UShort,
            ansCount: UShort,
            authCount: UShort,
            addCount: UShort,
            output: Output,
            buffer: ByteBuffer,
        ) {
            output.writeShort(value = qCount.toShort(), buffer = buffer)
            output.writeShort(value = ansCount.toShort(), buffer = buffer)
            output.writeShort(value = authCount.toShort(), buffer = buffer)
            output.writeShort(value = addCount.toShort(), buffer = buffer)
        }

        fun write(qCount: Int, ansCount: Int, authCount: Int, addCount: Int, buffer: ByteBuffer) {
            write(
                buffer = buffer,
                qCount = qCount.toUShort(),
                ansCount = ansCount.toUShort(),
                authCount = authCount.toUShort(),
                addCount = addCount.toUShort(),
            )
        }
    }
}
