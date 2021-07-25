package pw.binom.db

enum class TransactionMode {
    SERIALIZABLE,
    REPEATABLE_READ,
    READ_COMMITTED,
    READ_UNCOMMITTED,
}