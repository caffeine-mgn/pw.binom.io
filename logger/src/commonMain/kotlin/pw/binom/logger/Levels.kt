package pw.binom.logger

private object INFO_LEVEL : Logger.Level {
    override val name: String
        get() = "I"
    override val priority: UInt
        get() = 800u
}

val Logger.Companion.INFO: Logger.Level
    get() = INFO_LEVEL

fun Logger.info(text: String, exception: Throwable? = null) {
    log(Logger.INFO, text, exception)
}

fun Logger.info(exception: Throwable) {
    log(Logger.INFO, null, exception)
}

private object WARN_LEVEL : Logger.Level {
    override val name: String
        get() = "W"
    override val priority: UInt
        get() = 900u
}

val Logger.Companion.WARNING: Logger.Level
    get() = WARN_LEVEL

fun Logger.warn(text: String, exception: Throwable? = null) {
    log(Logger.WARNING, text, exception)
}

fun Logger.warn(exception: Throwable? = null) {
    log(Logger.WARNING, null, exception)
}

private object SEVERE_LEVEL : Logger.Level {
    override val name: String
        get() = "S"
    override val priority: UInt
        get() = 1000u
}

val Logger.Companion.SEVERE: Logger.Level
    get() = SEVERE_LEVEL

fun Logger.severe(text: String, exception: Throwable? = null) {
    log(Logger.SEVERE, text, exception)
}

fun Logger.severe(exception: Throwable? = null) {
    log(Logger.SEVERE, null, exception)
}