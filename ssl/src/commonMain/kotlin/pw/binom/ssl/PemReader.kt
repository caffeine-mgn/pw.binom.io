package pw.binom.ssl

import pw.binom.base64.Base64DecodeAppendable
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.Closeable
import pw.binom.io.Reader
import pw.binom.io.readln

class PemReader(private val reader: Reader) : Closeable {
    override fun close() {
        reader.close()
    }

    fun read(): PemObject? {
        var line = reader.readln() ?: return null

        if (!line.startsWith("-----BEGIN ") || !line.endsWith("-----"))
            throw IllegalStateException("Invalid line $line")
        val type = line.removePrefix("-----BEGIN ").removeSuffix("-----")
        val o = ByteArrayOutputStream()
        val b = Base64DecodeAppendable(o)

        while (true) {
            line = reader.readln() ?: break
            if (line == "-----END $type-----")
                break
            b.append(line)
        }

        return PemObject(type, o.toByteArray())
    }

    class PemObject(val type: String, val date: ByteArray)
}