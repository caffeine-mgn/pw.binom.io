package pw.binom.ssl

import pw.binom.Base64EncodeOutputStream

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
        val o = Base64EncodeOutputStream(ap)
        appendable.append("-----BEGIN ").append(type).append("-----\n")
        o.write(data)
        o.flush()
        o.close()
        if (ap.lineCount > 0)
            appendable.append("\n")
        appendable.append("-----END $type-----\n")
    }
}