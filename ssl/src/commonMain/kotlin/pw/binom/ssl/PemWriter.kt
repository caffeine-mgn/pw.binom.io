package pw.binom.ssl

import pw.binom.base64.Base64EncodeOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.writeByte

class PemWriter(val appendable: Appendable) {
    fun write(type: String, data: ByteArray) {
        val ap = object : Appendable {
            var lineCount = 0
            override fun append(value: Char): Appendable {
                if (lineCount >= 64) {
                    appendable.append("\n")
                    lineCount = 0
                }
                lineCount++
                appendable.append(value)
                return this
            }

            override fun append(value: CharSequence?): Appendable {
                value?.forEach {
                    append(it)
                }
                return this
            }

            override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
                value ?: return this
                for (i in startIndex..endIndex) {
                    append(value[i])
                }
                return this
            }
        }
        val o = Base64EncodeOutput(ap)
        ByteBuffer(1).use { buf ->
            appendable.append("-----BEGIN ").append(type).append("-----\n")
            data.forEach {
                o.writeByte(buf, it)
            }
            o.flush()
            o.close()
            if (ap.lineCount > 0) {
                appendable.append("\n")
            }
            appendable.append("-----END $type-----\n")
        }
    }
}
