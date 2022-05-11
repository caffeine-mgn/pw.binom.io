package pw.binom.db.postgresql.async

import pw.binom.readByte

object InformationParser {
    suspend fun readTo(ctx: PackageReader, map: MutableMap<Char, String>) {
        while (true) {
            val kind = ctx.input.readByte(ctx.buf16)

            if (kind == 0.toByte()) {
                break
            }

            map[kind.toInt().toChar()] = ctx.readCString()
        }
    }
}
