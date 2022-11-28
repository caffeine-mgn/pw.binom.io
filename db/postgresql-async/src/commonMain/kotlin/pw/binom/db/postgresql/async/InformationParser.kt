package pw.binom.db.postgresql.async

object InformationParser {
    suspend fun readTo(ctx: PackageReader, map: MutableMap<Char, String>) {
        while (true) {
            val kind = ctx.readByte()

            if (kind == 0.toByte()) {
                break
            }

            map[kind.toInt().toChar()] = ctx.readCString()
        }
    }
}
