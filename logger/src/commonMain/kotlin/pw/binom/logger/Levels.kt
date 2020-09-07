package pw.binom.logger

import kotlin.native.concurrent.SharedImmutable

private object INFO_LEVEL : Logger.Level {
    override val name: String
        get() = "I"
    override val priority: UInt
        get() = 800u
}

val Logger.INFO: Logger.Level
    get() = INFO_LEVEL

fun Logger.LoggerImpl.info(text: String) {
    log(Logger.INFO, text, null)
}

private object WARN_LEVEL : Logger.Level {
    override val name: String
        get() = "W"
    override val priority: UInt
        get() = 900u
}

val Logger.WARNING: Logger.Level
    get() = WARN_LEVEL

fun Logger.LoggerImpl.warn(text: String) {
    log(Logger.WARNING, text, null)
}

private object SEVERE_LEVEL : Logger.Level {
    override val name: String
        get() = "S"
    override val priority: UInt
        get() = 1000u
}

val Logger.SEVERE: Logger.Level
    get() = SEVERE_LEVEL

fun Logger.LoggerImpl.severe(text: String) {
    log(Logger.SEVERE, text, null)
}

fun Logger.LoggerImpl.severe(exception: Throwable) {
    log(Logger.SEVERE, null, exception)
}