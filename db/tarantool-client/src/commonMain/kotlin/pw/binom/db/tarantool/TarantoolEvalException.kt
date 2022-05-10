package pw.binom.db.tarantool

class TarantoolEvalException(val script: String, val errorMessage: String?) : TarantoolException() {
    override val message: String?
        get() =
            if (errorMessage == null) {
                "Can't execute \"$script\""
            } else {
                "Can't execute \"$script\": $errorMessage"
            }
}
