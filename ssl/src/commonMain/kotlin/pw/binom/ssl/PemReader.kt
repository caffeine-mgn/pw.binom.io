package pw.binom.ssl

import pw.binom.base64.Base64DecodeAppendable
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Closeable
import pw.binom.io.Reader
import pw.binom.io.use

class PemReader(private val reader: Reader) : Closeable {
    override fun close() {
        reader.close()
    }

    fun read(): PemObject? {
        var line = reader.readln() ?: return null

        if (!line.startsWith("-----BEGIN ") || !line.endsWith("-----"))
            throw IllegalStateException("Invalid line $line")
        val type = line.removePrefix("-----BEGIN ").removeSuffix("-----")
        val o = ByteArrayOutput()
        return Base64DecodeAppendable(o).use { b ->
            while (true) {
                line = reader.readln() ?: break
                if (line == "-----END $type-----")
                    break
                b.append(line)
            }
            o.trimToSize()
            o.data.clear()
            val array = o.data.toByteArray()
            o.close()
            PemObject(type, array)
        }
    }

    class PemObject(val type: String, val date: ByteArray)
}