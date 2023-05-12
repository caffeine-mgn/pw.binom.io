package pw.binom.dns

import pw.binom.dns.protocol.QueryPackage
import pw.binom.dns.protocol.ResourcePackage
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.readShort
import pw.binom.writeShort

/*
 *    +---------------------+
 *    |        Header       |
 *    +---------------------+
 *    |       Question      | the question for the name server
 *    +---------------------+
 *    |        Answer       | RRs answering the question
 *    +---------------------+
 *    |      Authority      | RRs pointing toward an authority
 *    +---------------------+
 *    |      Additional     | RRs holding additional information
 *    +---------------------+
 */

class DnsPackage(
    val header: Header,
    val question: List<QueryPackage>,
    val answer: List<ResourcePackage>,
    val authority: List<ResourcePackage>,
    val additional: List<ResourcePackage>,
) {
    val sizeBytes
        get() = Header.SIZE_BYTES +
            PayloadCounters.SIZE_BYTES +
            question.sumOf { it.sizeBytes } +
            answer.sumOf { it.sizeBytes } +
            authority.sumOf { it.sizeBytes } +
            additional.sumOf { it.sizeBytes }

    companion object {
        suspend fun read(input: AsyncInput, buffer: ByteBuffer): DnsPackage {
            val len = input.readShort(buffer).toInt()
            buffer.reset(0, len)
            input.readFully(buffer)
            buffer.flip()
            val header = Header()
            header.read(buffer)
            val question = ArrayList<QueryPackage>()
            val answer = ArrayList<ResourcePackage>()
            val authority = ArrayList<ResourcePackage>()
            val additional = ArrayList<ResourcePackage>()
            var qCount = 0
            var ansCount = 0
            var authCount = 0
            var addCount = 0
            PayloadCounters.read(
                buffer = buffer,
                qCount = { qCount = it.toInt() },
                ansCount = { ansCount = it.toInt() },
                authCount = { authCount = it.toInt() },
                addCount = { addCount = it.toInt() },
            )
            repeat(qCount) {
                val q = QueryPackage()
                q.read(buffer)
                question += q
            }
            repeat(ansCount) {
                val r = ResourcePackage()
                r.read(buffer)
                answer += r
            }
            repeat(authCount) {
                val r = ResourcePackage()
                r.read(buffer)
                authority += r
            }
            repeat(addCount) {
                val r = ResourcePackage()
                r.read(buffer)
                additional += r
            }

            return DnsPackage(
                header = header,
                question = question,
                answer = answer,
                authority = authority,
                additional = additional,
            )
        }
    }

    fun createBuffer(buffer: ByteBuffer) {
        buffer.writeShort(sizeBytes.toShort())
        header.write(buffer)
        question.forEach {
            it.write(buffer)
        }
        answer.forEach {
            it.write(buffer)
        }
        authority.forEach {
            it.write(buffer)
        }
        additional.forEach {
            it.write(buffer)
        }
    }
}
