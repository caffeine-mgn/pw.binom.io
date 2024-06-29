package pw.binom.compression.tar

enum class TarEntityType(internal val num: Byte) {
    REGULAR(47),
    NORMAL(48),
    HARDLINK(49),
    SYMLINK(50),
    CHAR(51),
    BLOCK(52),
    DIRECTORY(53),
    FIFO(54),
    CONTIGUOUS(55),
    ;

    companion object {
        private val byCode = entries.associateBy { it.num }
        fun findByCode(code: Byte) = byCode[code]
    }
}
