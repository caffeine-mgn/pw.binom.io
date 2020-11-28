package pw.binom.logger

private object INFO_LEVEL : Logger.Level {
    override val name: String
        get() = "I"
    override val priority: UInt
        get() = 800u
}

val Logger.Companion.INFO: Logger.Level
    get() = INFO_LEVEL

fun Logger.info(text: String) {
    log(Logger.INFO, text, null)
}

private object WARN_LEVEL : Logger.Level {
    override val name: String
        get() = "W"
    override val priority: UInt
        get() = 900u
}

val Logger.Companion.WARNING: Logger.Level
    get() = WARN_LEVEL

fun Logger.warn(text: String) {
    log(Logger.WARNING, text, null)
}

private object SEVERE_LEVEL : Logger.Level {
    override val name: String
        get() = "S"
    override val priority: UInt
        get() = 1000u
}

val Logger.Companion.SEVERE: Logger.Level
    get() = SEVERE_LEVEL

fun Logger.severe(text: String) {
    log(Logger.SEVERE, text, null)
}

fun Logger.severe(exception: Throwable) {
    log(Logger.SEVERE, null, exception)
}