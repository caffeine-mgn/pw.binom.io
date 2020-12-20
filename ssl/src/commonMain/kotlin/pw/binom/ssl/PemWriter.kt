package pw.binom.ssl

import pw.binom.ByteBuffer
import pw.binom.base64.Base64EncodeOutput
import pw.binom.writeByte

class PemWriter(val appendable: Appendable) {
    fun write(type: String, data: ByteArray) {
        val ap = object : Appendable {
            var lineCount = 0
            override fun append(c: Char): Appendable {
                if (lineCount >= 64) {
                    appendable.append("\n")
                    lineCount = 0
                }
                lineCount++
                appendable.append(c)
                return this
            }

            override fun append(csq: CharSequence?): Appendable {
                csq?.forEach {
                    append(it)
                }
                return this
            }

            override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
                csq ?: return this
                for (i in start..end) {
                    append(csq[i])
                }
                return this
            }

        }
        val o = Base64EncodeOutput(ap)
        val buf = ByteBuffer.alloc(1)
        try {
            appendable.append("-----BEGIN ").append(type).append("-----\n")
            data.forEach {
                o.writeByte(buf, it)
            }
            o.flush()
            o.close()
            if (ap.lineCount > 0)
                appendable.append("\n")
            appendable.append("-----END $type-----\n")
        } finally {
            buf.close()
        }
    }
}